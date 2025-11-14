# Musinsa Point System

## 1. 프로젝트 개요

본 프로젝트는 무신사페이먼츠 백엔드 엔지니어 과제 전형의 요구사항에 따라 구현된 무료 포인트 시스템 API입니다.

### 주요 기능
- **인증/인가**: 회원가입/로그인을 통해 JWT Access/Refresh Token을 발급받고, 토큰으로 API를 호출합니다.
- **포인트 적립**: 사용자는 포인트를 적립할 수 있습니다.
- **포인트 적립 취소**: 특정 적립 내역을 취소할 수 있습니다. (단, 사용되지 않은 경우에만)
- **포인트 사용**: 주문 시 포인트를 사용할 수 있습니다.
- **포인트 사용 취소**: 사용한 포인트를 전체 또는 부분 취소할 수 있습니다.

### 개발 환경
- Java 21
- Spring Boot 3.x
- H2 Database
- Gradle

## 2. ERD (Entity-Relationship Diagram)

엔티티 관계는 다음과 같습니다.

- **`PointConfig`**: 포인트 시스템의 주요 설정값(키-값)을 관리합니다.
  - `configKey`: 설정 키 (e.g., `MAX_ACCUMULATE_AMOUNT`)
  - `configValue`: 설정 값

- **`PointEarning`**: 개별 포인트 '적립' 단위를 나타냅니다.
  - `userId`: 포인트를 소유한 사용자 ID
  - `amount`: 적립된 총 금액
  - `remainingAmount`: 사용하고 남은 금액
  - `expirationDate`: 만료일
  - `isManual`: 관리자에 의한 수기 지급 여부

- **`PointUsage`**: 개별 포인트 '사용' 단위를 나타냅니다.
  - `userId`: 포인트를 사용한 사용자 ID
  - `orderNumber`: 포인트를 사용한 주문 번호
  - `amount`: 사용된 총 금액
  - `cancellableAmount`: 사용 취소 가능한 남은 금액

- **`PointUsageDetail`**: 어떤 적립(Earning)이 어떤 사용(Usage)에 얼마나 쓰였는지 연결하는 조인 테이블입니다.
  - `pointEarningId`: `PointEarning`의 ID (FK)
  - `pointUsageId`: `PointUsage`의 ID (FK)
  - `amount`: 해당 관계에서 사용된 금액

- **`PointHistory`**: 모든 포인트 관련 트랜잭션(적립, 사용, 취소 등)의 로그를 기록합니다.
  - `type`: 트랜잭션 타입 (e.g., `ACCUMULATE`, `USE`)
  - `pointKey`: 관련 `PointEarning` 또는 `PointUsage`의 키
  - `amount`: 변동된 금액
  - `balance`: 트랜잭션 후의 총 잔액

## 3. API 명세

### 인증 API (`/api/auth`)
- `POST /signup`: 이메일/비밀번호로 회원가입
- `POST /login`: 로그인 후 `accessToken`, `refreshToken` 발급
- `POST /refresh`: 유효한 Refresh Token으로 토큰 재발급

모든 포인트 API는 `Authorization: Bearer {accessToken}` 헤더가 필요합니다.

### 포인트 API (`/api/points`)

### `POST /api/points/accumulate`
포인트를 적립합니다.

**Request Body:**
```json
{
  "userId": 1,
  "amount": 10000,
  "isManual": false,
  "expirationDays": 365
}
```

### `POST /api/points/accumulate-cancel`
포인트 적립을 취소합니다.

**Request Body:**
```json
{
  "pointKey": "GENERATED_POINT_KEY"
}
```

### `POST /api/points/use`
포인트를 사용합니다.

**Request Body:**
```json
{
  "userId": 1,
  "orderNumber": "ORDER-12345",
  "amount": 5000
}
```

### `POST /api/points/use-cancel`
포인트 사용을 취소합니다.

**Request Body:**
```json
{
  "pointKey": "GENERATED_POINT_KEY",
  "amount": 2000
}
```

### `GET /api/points/balance/{userId}`
사용자의 현재 포인트 잔액을 조회합니다.

### `GET /api/points/history/{userId}`
사용자의 포인트 변동 내역을 조회합니다.

## 4. 빌드 및 실행 방법

### 사전 설정: .env 파일 생성
애플리케이션을 실행하기 전에, 프로젝트 루트 디렉토리에 `.env` 파일을 생성하고 데이터베이스 접속 정보를 설정해야 합니다.

```
# .env
DB_URL=jdbc:h2:mem:pointdb
DB_USERNAME=sa
DB_PASSWORD=
JWT_SECRET=please_change_this_secret_key
```

### 빌드
프로젝트 루트 디렉토리에서 아래 명령어를 실행하여 프로젝트를 빌드합니다.
```bash
./gradlew build
```

### 실행
빌드된 JAR 파일을 다음 명령어로 실행합니다.
```bash
java -jar build/libs/point-0.0.1-SNAPSHOT.jar
```

애플리케이션이 실행되면 `http://localhost:8080` 에서 접속할 수 있습니다.
H2 데이터베이스 콘솔은 `http://localhost:8080/h2-console` 에서 접근 가능합니다.
- JDBC URL: `jdbc:h2:mem:pointdb`
- User Name: `sa`
- Password: (비워두기)
