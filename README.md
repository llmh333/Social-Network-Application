# HIT Chill and Chill



# Backend

*(Toàn bộ mã nguồn backend sẽ nằm ở thư mục này)*

---

## Yêu Cầu Hệ Thống

### Những yêu cầu về môi trường chạy dự án:

- **Java**: 17 trở lên  
- **Maven**: 3.8.7 hoặc mới hơn  
- **Database**: MySQL

---

## Cấu Trúc Thư Mục

###Cấu trúc dự án

<pre> ``` 
project
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── example
│   │   │           └── projectbase
│   │   │               ├── aop
│   │   │               ├── base
│   │   │               ├── config
│   │   │               ├── constant
│   │   │               ├── controller
│   │   │               ├── domain
│   │   │               ├── exception
│   │   │               ├── job
│   │   │               ├── repository
│   │   │               ├── security
│   │   │               ├── service
│   │   │               ├── util
│   │   │               └── validator
│   ├── resources
│   │   ├── i18n
│   │   ├── static
│   │   └── templates
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       └── application-prod.properties
│   └── test
├── pom.xml
└── README.md


 ``` </pre>

 #### Giải thích từng thư mục 
 - **aop** : Xử lý log, thống kê và cập nhật hoạt động người dùng bằng AOP.
 - **base**: Cung cấp các lớp nền để chuẩn hóa phản hồi API.
 - **config**: Chứa các cấu hình của ứng dụng (các bean, cài đặt bảo mật, cấu hình database).
 - **constant**: Khai báo các hằng số dùng toàn hệ thống.
 - **controller**: Lớp xử lý yêu cầu HTTP.
 - **domain**: Chứa các entity ánh xạ với cơ sở dữ liệu.
 - **exception**: Định nghĩa và xử lý các ngoại lệ tùy chỉnh.
 - **repository**: Tầng giao tiếp với cơ sở dữ liệu.
 - **security**: Cấu hình và xử lý xác thực, phân quyền (JWT, OAuth2,...).
 - **service**: Xử lý logic của ứng dụng.
 - **util**: Các tiện ích dùng chung.
 - **validator**: Chứa các custom annotation và class để kiểm tra dữ liệu đầu vào.
 - **i18n**: Chứa các file đa ngôn ngữ (thông báo lỗi, thành công...) phục vụ việc quốc tế hóa ứng dụng.
 - **static**: Chứa các tài nguyên tĩnh.
 - **application.properties**: File cấu hình gốc.
 - **application-dev.properties**: File cấu hình dùng khi chạy môi trường phát triển.
 - **application-prod.properties**: File cấu hình dùng khi chạy môi trường thật.
 - **pom.xml**: File cấu hình Maven.

 ## Hệ thống
 ### Thiết kế theo kiến trúc phân lớp như hình vẽ bên dưới:
![ảnh minh hoạ](https://drive.google.com/file/d/1eYo8nHt4MjYhYCqe-aSZR1rWT_37w_jJ/view?usp=sharing)

### Auth Controller

- Auth Controller: Xử lý đăng nhập, đăng ký, đăng xuất, xác thực người dùng.
    - Sử dụng JWT để phân quyền. Dùng access token và refresh token.
    - Sử dụng Redis để lưu session và theo dõi trạng thái người dùng.
    - Sử dụng MySQL để lưu thông tin người dùng và phiên đăng nhập.
    - Hỗ trợ đăng nhập bằng Google, Facebook (OAuth2).

### Comment Controller

- Comment Controller: Quản lý chức năng bình luận cho người dùng.
    - Sử dụng MySQL để lưu trữ bình luận.
    - Sử dụng JPA để truy vấn, phân trang và quản lý mối quan hệ giữa bình luận và các thực thể liên quan (ví dụ: bài viết, hồ sơ,...).
    - Hỗ trợ phân quyền bằng Spring Security (chỉ cho phép người dùng đã đăng nhập được bình luận hoặc xoá bình luận của mình).
    - Sử dụng Spring Boot làm framework chính.

### Media Controller

- Media Controller: Quản lý upload, xoá và truy xuất file media (ảnh, video, audio).
    - Sử dụng Cloudinary để lưu trữ media.
    - Sử dụng MySQL để lưu metadata file.
    - Tích hợp Cloudinary API để xoá file khỏi server.
    - Sử dụng Spring Boot.

### OAuth Controller

- OAuth Controller: Xử lý đăng nhập bằng Google/Facebook thông qua OAuth2.
    - Trích xuất thông tin từ OAuth2User và tạo người dùng mới nếu chưa tồn tại.
    - Sử dụng Spring Security OAuth2 để xử lý đăng nhập.
    - Sử dụng MySQL để lưu thông tin người dùng.
    - Tạo token và lưu vào cookie sau khi đăng nhập thành công.

### Follow Controller

- Follow Controller: Quản lý theo dõi giữa người dùng với nhau.
    - Sử dụng MySQL để lưu thông tin mối quan hệ follow.
    - Sử dụng JPA để phân trang và truy vấn người theo dõi/người đang theo dõi.
    - Xác thực người dùng qua Spring Security.
    - Sử dụng Spring Boot.

### OtpForgotPassword Controller

- OtpForgotPassword Controller: Xử lý gửi mã OTP, xác minh OTP và đổi mật khẩu mới khi người dùng quên mật khẩu.
    - Sử dụng MySQL để lưu thông tin OTP và người dùng.
    - Sử dụng Spring Scheduler để xóa tự động OTP hết hạn.
    - Sử dụng Spring Security để mã hóa mật khẩu mới.
    - Sử dụng Thymeleaf để render email HTML gửi OTP.
    - Sử dụng MailService để gửi email OTP.

### Post Controller

- Post Controller: Quản lý các chức năng liên quan đến bài viết như tạo, đọc, xóa và tìm kiếm.
    - Sử dụng MySQL để lưu trữ thông tin bài viết.
    - Sử dụng JPA để thao tác và phân trang dữ liệu.
    - Sử dụng AWS S3 để lưu trữ file media (hình ảnh, audio, video).
    - Sử dụng Kafka để gửi dữ liệu kiểm duyệt nội dung media.
    - Hỗ trợ xử lý phân quyền với Spring Security.
    - Sử dụng Redis để gợi ý bài viết thịnh hành theo người dùng.

### Reaction Controller

- Reaction Controller: Quản lý hành động cảm xúc của người dùng đối với bài viết (thả like, love, haha...).
    - Sử dụng MySQL để lưu trữ dữ liệu reaction.
    - Sử dụng JPA để thao tác và phân trang dữ liệu reaction theo bài viết.
    - Sử dụng Spring Security để xác thực người dùng khi tạo hoặc hủy cảm xúc.

### Role Controller

- Role Controller: Quản lý phân quyền người dùng thông qua các vai trò (role).
    - Sử dụng Spring Security để kiểm soát quyền truy cập các API (chỉ Admin mới được tạo, cập nhật, xóa role).
    - Sử dụng MySQL để lưu trữ thông tin role.
    - Sử dụng JPA để truy vấn role và kiểm tra ràng buộc với người dùng.

### Share Controller

- Share Controller: Cho phép người dùng chia sẻ lại bài viết đã có.
    - Sử dụng Spring Security để xác thực người dùng trước khi chia sẻ.
    - Dữ liệu bài viết chia sẻ được lưu trữ trong MySQL, sử dụng JPA để thao tác.

### User Controller

- User Controller: Quản lý người dùng – tạo mới, cập nhật thông tin, xóa tài khoản, đổi mật khẩu, lấy người dùng hiện tại.
    - Sử dụng Spring Security để phân quyền và kiểm tra quyền truy cập.
    - Sử dụng Spring Data JPA để truy vấn dữ liệu và phân trang.
    
### UserSetting Controller

- UserSetting Controller: Quản lý cài đặt cá nhân của người dùng như giao diện, ngôn ngữ.
    - Sử dụng Spring Security để phân quyền và kiểm tra quyền truy cập.
    - Dùng MySQL để lưu trữ cài đặt người dùng.
    - Sử dụng Spring Data JPA để truy vấn và cập nhật dữ liệu.
    - Kiểm tra xác thực người dùng bằng SecurityContextHolder.

## Công nghệ sử dụng

Dự án sử dụng các công nghệ sau:
-Ngôn ngữ: Java 17
-Framework: Spring Boot 3.x
-ORM: Spring Data JPA + Hibernate
-Bảo mật: Spring Security 6 kết hợp với JWT (JSON Web Token)
-Cơ sở dữ liệu: MySQL 8
-Build tool: Maven
-Ghi log: SLF4J + Logback
-Kiểm thử API: Postman, Swagger UI

## Pre-requisites - Yêu cầu

- Cần có các công cụ sau để cài đặt và chạy một local server:
  - [Docker](https://www.docker.com/)
  - [Docker Compose](https://docs.docker.com/compose/)

## Hướng dẫn cài đặt

- cd vào thư mục backend:
<pre> ``` cd backend ``` </pre>
-Sử dụng Docker để xây dựng image từ Dockerfile. Chạy lệnh sau trong thư mục chứa Dockerfile:
<pre> ``` docker build -t project-base . ``` </pre>
-Sau khi xây dựng image, bạn có thể chạy container bằng lệnh sau:
<pre> ``` docker run -d -p 8080:8080 --name my-app project-base:1.0 ``` </pre>
- `-d`: Chạy container ở chế độ nền (detached mode).
- `-p 8080:8080`: Chuyển tiếp cổng 8000 từ máy host sang cổng 8080 của container.

##PORT BINDING
- Sau khi chạy xong, các service sẽ được chạy trên các port như sau:

| Service     | PORT      |
|-------------|-----------|
| API Gateway | 8080:8080 |










