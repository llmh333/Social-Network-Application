import json, io, requests
import cv2
import torch
from PIL import Image
from kafka import KafkaConsumer, KafkaProducer
from transformers import AutoImageProcessor, AutoModelForImageClassification
from concurrent.futures import ThreadPoolExecutor, as_completed
import tempfile
import os
import hashlib
from dotenv import load_dotenv

load_dotenv()
KAFKA_BOOTSTRAP_SERVERS = os.getenv('KAFKA_BOOTSTRAP_SERVERS', 'localhost:9092')
KAFKA_CONSUMER_TOPIC = os.getenv('KAFKA_CONSUMER_TOPIC', 'moderation_requests')
KAFKA_CONSUMER_GROUP_ID = os.getenv('KAFKA_CONSUMER_GROUP_ID', 'python-moderation-group')
KAFKA_PROCESSING_TOPIC = os.getenv('KAFKA_PROCESSING_TOPIC', 'processing_requests')
KAFKA_RESULT_TOPIC = os.getenv('KAFKA_RESULT_TOPIC', 'moderation_results')
MODEL_NAME = "Falconsai/nsfw_image_detection"

VIDEO_CHECK_INTERVAL_SECONDS = 5
MAX_VIDEO_DURATION = 300
BATCH_SIZE = 4
MAX_WORKERS = 3
FRAME_RESIZE_HEIGHT = 224
EARLY_STOP_THRESHOLD = 0.8

def load_moderation_model():
    """Tải và tối ưu mô hình kiểm duyệt."""
    print("Đang tải mô hình kiểm duyệt...")
    device = "cpu"
    processor = AutoImageProcessor.from_pretrained(MODEL_NAME)
    model = AutoModelForImageClassification.from_pretrained(MODEL_NAME).to("cpu")

    model.eval()

    print(f"Đã tải xong mô hình. Đang chạy trên thiết bị: {device}")
    return processor, model, device

def get_cache_key(url):
    """Tạo cache key từ URL."""
    return hashlib.md5(url.encode()).hexdigest()

def preprocess_frame(frame, target_height=FRAME_RESIZE_HEIGHT):
    """Tiền xử lý frame để tăng tốc."""
    height, width = frame.shape[:2]
    if height > target_height:
        scale = target_height / height
    new_width = int(width * scale)
    frame = cv2.resize(frame, (new_width, target_height), interpolation=cv2.INTER_LINEAR)
    return cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)

def batch_predict(frames, processor, model, device):
    """Dự đoán NSFW cho một loạt frame."""
    if not frames: return []
    images = [Image.fromarray(frame) for frame in frames]
    inputs = processor(images=images, return_tensors="pt").to(device)

    if device == "cuda":
        inputs = {k: v.half() if v.dtype == torch.float32 else v for k, v in inputs.items()}

    with torch.no_grad():
        logits = model(**inputs).logits
        probabilities = torch.nn.functional.softmax(logits, dim=-1)

    predictions = []
    for prob, logit in zip(probabilities, logits):
        pred_class = model.config.id2label[logit.argmax(-1).item()]
        confidence = prob.max().item()
        predictions.append((pred_class, confidence))
    return predictions

def moderate_image_from_url(url, processor, model, device):
    """Kiểm duyệt một ảnh từ URL với cache. (Hàm này giữ nguyên)"""
    cache_key = get_cache_key(url)
    if cache_key in processed_cache:
        return processed_cache[cache_key]
    try:
        response = requests.get(url, stream=True, timeout=10)
        response.raise_for_status()
        with Image.open(io.BytesIO(response.content)) as image:
            inputs = processor(images=image.convert("RGB"), return_tensors="pt").to(device)
            if device == "cuda":
                inputs = {k: v.half() if v.dtype == torch.float32 else v for k, v in inputs.items()}
            
            with torch.no_grad():
                logits = model(**inputs).logits
            
            predicted_label = model.config.id2label[logits.argmax(-1).item()]
            status = 'nsfw' if predicted_label == 'nsfw' else 'safe'
            result = {"url": url, "status": status}
            
            if len(processed_cache) < CACHE_SIZE: processed_cache[cache_key] = result
            return result
    except Exception as e:
        return {"url": url, "status": "error", "message": f"Lỗi xử lý ảnh: {e}"}
def moderate_video_from_url(url, processor, model, device):
    """Kiểm duyệt một video bằng cách tải về file tạm, xử lý, rồi xóa."""
    cache_key = get_cache_key(url)
    if cache_key in processed_cache:
        return processed_cache[cache_key]
    temp_video_path = None
    try:
        # 1. Tải video về một file tạm
        print(f"Đang tải video từ: {url}")
        with tempfile.NamedTemporaryFile(suffix=".mp4", delete=False) as temp_f:
            temp_video_path = temp_f.name
        
        with requests.get(url, stream=True, timeout=120) as r: # Tăng timeout để tải file
            r.raise_for_status()
            with open(temp_video_path, 'wb') as f:
                for chunk in r.iter_content(chunk_size=8192):
                    f.write(chunk)
        print(f"Đã tải xong video vào file tạm: {temp_video_path}")

        # 2. Xử lý video từ file tạm đã tải về
        cap = cv2.VideoCapture(temp_video_path)
        if not cap.isOpened():
            raise IOError("Không thể mở file video tạm.")
        
        fps = cap.get(cv2.CAP_PROP_FPS)
        total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
        duration = total_frames / fps if fps > 0 else 0
        
        if duration > MAX_VIDEO_DURATION:
            raise ValueError(f"Video quá dài ({duration:.1f}s).")
        
        frame_interval = int(fps * VIDEO_CHECK_INTERVAL_SECONDS) if fps > 0 else 30
        frame_count, nsfw_detected = 0, False
        frame_buffer = []
        
        while True:
            ret, frame = cap.read()
            if not ret: break

            if frame_count % frame_interval == 0:
                frame_buffer.append(preprocess_frame(frame))
                
                if len(frame_buffer) >= BATCH_SIZE:
                    predictions = batch_predict(frame_buffer, processor, model, device)
                    for pred_class, confidence in predictions:
                        if pred_class == 'nsfw' and (confidence > EARLY_STOP_THRESHOLD or (not nsfw_detected and confidence > 0.5)):
                            nsfw_detected = True
                    frame_buffer = []
                    if nsfw_detected: break
            frame_count += 1
        
        if frame_buffer and not nsfw_detected:
            predictions = batch_predict(frame_buffer, processor, model, device)
            for pred_class, confidence in predictions:
                if pred_class == 'nsfw' and confidence > 0.5:
                    nsfw_detected = True
                    break
        
        cap.release()
        status = "nsfw_detected" if nsfw_detected else "safe"
        result = {"url": url, "status": status}
        
        if len(processed_cache) < CACHE_SIZE: processed_cache[cache_key] = result
        return result

    except Exception as e:
        return {"url": url, "status": "error", "message": f"Lỗi xử lý video: {e}"}
    finally:
        # 3. Luôn luôn dọn dẹp file tạm sau khi xử lý
        if temp_video_path and os.path.exists(temp_video_path):
            os.remove(temp_video_path)
            print(f"Đã xóa file tạm: {temp_video_path}")
            
def process_multiple_urls_parallel(urls, media_type, processor, model, device):
    """Xử lý nhiều URL đồng thời và trả về một quyết định chung."""
    results = []
    func_to_call = moderate_image_from_url if media_type == 'image' else moderate_video_from_url
    with ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
        futures = {executor.submit(func_to_call, url, processor, model, device): url for url in urls}
        for future in as_completed(futures):
            try:
                results.append(future.result(timeout=MAX_VIDEO_DURATION + 60)) # Timeout lớn hơn để bao gồm cả thời gian tải
            except Exception as e:
                results.append({"url": futures[future], "status": "error", "message": f"Lỗi thread: {e}"})

    final_decision = 'safe'
    if any(r.get('status') == 'nsfw' or r.get('status') == 'nsfw_detected' for r in results):
        final_decision = 'nsfw_detected'
    elif any(r.get('status') == 'error' for r in results):
        final_decision = 'error'

    return final_decision, results

def main():
    """Hàm chính để chạy consumer."""
    processor, model, device = load_moderation_model()
    print("\nĐang khởi tạo Kafka...")
    try:
        consumer = KafkaConsumer(
            KAFKA_CONSUMER_TOPIC,
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            group_id=KAFKA_CONSUMER_GROUP_ID,
            auto_offset_reset='earliest',
            value_deserializer=lambda m: m.decode('utf-8'),
        )
        
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP_SERVERS,
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
    except Exception as e:
        print(f"Lỗi khi kết nối tới Kafka: {e}")
        return

    print(f"Sẵn sàng lắng nghe trên topic '{KAFKA_CONSUMER_TOPIC}'...")

    try:
        for message in consumer:
            print("-" * 60)
            print(f"Nhận message từ partition {message.partition}, offset {message.offset}")
            
            try:
                data = json.loads(message.value)
                print(f"Message: {data}")
                post_id = data.get("postId")
                media_type = data.get("type", "").lower()
                urls_value = data.get("s3Urls")
                contentType = data.get("contentType", "")
                singerName = data.get("singer_name", "")
                userId = data.get("user_id", "")

                if not all([post_id, media_type, urls_value]):
                    print(f"Lỗi: Message không hợp lệ, thiếu trường. Bỏ qua. Message: {data}")
                    continue

                url_list = urls_value if isinstance(urls_value, list) else [urls_value]
                print(f"Yêu cầu kiểm duyệt: postId={post_id}, Type={media_type}, URLs={len(url_list)}")
                
                final_decision, detailed_results = process_multiple_urls_parallel(url_list, media_type, processor, model, device)
                
                print("--- Kết quả chi tiết ---")
                for res in detailed_results: print(f"  {res}")
                print("-----------------------")

                if final_decision == 'safe':
                    processing_message = {"postId": post_id, "s3Urls": urls_value, "contentType": contentType, "singerName": singerName, "userId": userId}
                    producer.send(KAFKA_PROCESSING_TOPIC, processing_message)
                    print(f"✅ [postId: {post_id}] An toàn. Đã chuyển tiếp đến topic '{KAFKA_PROCESSING_TOPIC}'.")
                
                elif final_decision == 'nsfw_detected':
                    result_message = {"postId": post_id, "status": "REJECTED", "details": detailed_results}
                    producer.send(KAFKA_RESULT_TOPIC, result_message)
                    print(f"❌ [postId: {post_id}] Bị từ chối. Đã gửi kết quả đến topic '{KAFKA_RESULT_TOPIC}'.")
                
                else: # Trường hợp 'error'
                    result_message = {"postId": post_id, "status": "FAILED", "details": detailed_results}
                    producer.send(KAFKA_RESULT_TOPIC, result_message)
                    print(f"⚠️ [postId: {post_id}] Lỗi xử lý. Đã gửi kết quả đến topic '{KAFKA_RESULT_TOPIC}'.")

                producer.flush()

            except json.JSONDecodeError:
                print(f"Lỗi: Không thể giải mã JSON từ message: {message.value}")
            except Exception as e:
                print(f"Đã xảy ra lỗi không mong muốn khi xử lý message: {e}")

    except KeyboardInterrupt:
        print("\nĐang dừng consumer và producer...")
    finally:
        if 'consumer' in locals(): consumer.close()
        if 'producer' in locals(): producer.close()
        print("Đã đóng kết nối Kafka.")
        
if name == "main":
    main()