# 🌐 Social Network Application

<p align="left">
	<a href="https://qloapps.com/download/"><img src="https://img.shields.io/badge/Java-17-yellowgreen" alt="Java"></a>
	<a href="https://docs.qloapps.com/"><img src="https://img.shields.io/badge/Spring_Boot-2.6.3-brightgreen" alt="Documentation"></a>
	<a href="https://qloapps.com/addons/"><img src="https://img.shields.io/badge/Addons-Plugins-blueviolet" alt="Addons"></a>
	<a href="https://qloapps.com/contact/"><img src="https://img.shields.io/badge/Kafka-3.x.x-blue" alt="Contact us"></a>
	<a href="/LICENSE.md"><img src="https://img.shields.io/badge/Redis Client-3.7.1-red" alt="License"></a>
</p>
 
A unified social networking ecosystem that consolidates photo sharing, video
streaming, and music integration, delivering a seamless all-in-one multimedia.
---

## 🛠️ Tech Stack
- **Backend**: Spring Boot 2.x, Spring Data JPA, Spring Security, Spring Apache Kafka, Spring Data Redis
- Frontend: ReactJS
- AI: Python, Transformers
- Databases: MySQL
- Cache: Redis
- Messaging: Kafka
- Orchestration: Docker & Docker Compose
- Cloud Networking: Cloudflare
- API testing: Postman, Swagger

---

## 📂 Project Structure
 ```text 
project
├── python-service
│   ├── .env-example
│   ├── .requirements.txt
│   ├── main.py
│   └── Dockerfile
├── BE
│   ├── src
│   │   └── main
│   │       ├── java
│   │       │    └── com.example.projectbase
│   │       │         ├── aop
│   │       │         ├── base
│   │       │         ├── config
│   │       │         ├── constant
│   │       │         ├── controller
│   │       │         ├── domain
│   │       │         ├── exception
│   │       │         ├── job
│   │       │         ├── repository
│   │       │         ├── security
│   │       │         ├── service
│   │       │         ├── util
│   │       │         └── validator
│   │       └── resources
│   │             ├── i18n
│   │             ├── static
│   │             ├── templates
│   │             ├── application.properties
│   │             ├── application-dev.properties
│   │             └── application-prod.properties
│   ├── .env-example
│   ├── Dockerfile
├── pom.xml
├── nginx.conf
├── docker-compose.yml
└── README.md
 ```


## 🚀 Hướng dẫn Cài đặt
Hệ thống yêu cầu đã cài đặt Docker và Docker Compose.  
**Clone kho mã nguồn:**
```bash
  git clone https://github.com/llmh333/Social-Network-Application.git
```

**Di chuyển vào thư mục Social-Network-Application:**
```bash
  cd Social-Network-Application
```

**Biên dịch và Khởi chạy Backend:**
```bash
  docker-compose up -d --build
```

**Truy cập Swagger để Test các API chức năng**:
```text
  http://localhost:8080/swagger-ui.html
```

## 📜 Giấy phép
Dự án này được cấp phép theo Giấy phép [MIT License](LINCENSE)










