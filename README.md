# 👵 노인 키오스크 연습 애플리케이션  
**Kiosk Practice App for Seniors – 햄버거 / 카페 / 영화관 / 식당**

---

## 📋 목차
1. [초기 제작 프롬프트](#초기-제작-프롬프트)  
2. [애플리케이션 개요](#애플리케이션-개요)  
3. [도메인(업종)별 시나리오](#도메인업종별-시나리오)  
4. [컴포넌트 사양서](#컴포넌트-사양서)  
5. [데이터 구조](#데이터-구조)  
6. [업무 흐름 & 모드 로직](#업무-흐름--모드-로직)  
7. [디자인 시스템 & 접근성](#디자인-시스템--접근성)  
8. [테스트 시나리오](#테스트-시나리오)  
9. [설치 & 실행](#설치--실행)  
10. [개선 프롬프트 예시](#개선-프롬프트-예시)  
11. [로드맵](#로드맵)  
12. [프로젝트 맥락](#프로젝트-맥락)

---

## 🎯 초기 제작 프롬프트

노인분들을 위한 키오스크 연습 애플리케이션을 만들어주세요.

주요 기능
연습 모드: 단계별 안내가 있는 가이드 모드

실전 모드: 랜덤 미션 기반 주문

학습 기록: localStorage에 실전 결과 저장(성공/실패, 주문내역, 시각)

도움말: 사용 방법/팁/FAQ

업종(도메인)
햄버거, 카페, 영화관, 식당(일반 한식·백반)

업종마다 실제 흐름이 다름

디자인 요구사항
모바일 최적화(max-w-md)

큰 글씨, 큰 버튼, 명확한 색상 대비

Material Design 느낌 + 모션 가이드

색상: 연습(파랑), 실전(초록), 업종별 테마

기술 스택
React + TypeScript + Vite

Tailwind CSS

shadcn/ui, lucide-react

motion/react (애니메이션)

localStorage (학습 기록 저장)

yaml
코드 복사

---

## 📱 애플리케이션 개요

| 구분 | 설명 |
|------|------|
| **목표 사용자** | 60대 이상, 키오스크에 익숙하지 않은 노인 |
| **핵심 가치** | 안전한 연습 환경, 단계별 학습, 실전 미션, 학습 피드백 |
| **접근성** | 큰 폰트, 큰 터치 영역, 색상 대비, 음성/시각 피드백 |

---

## 🗺 도메인(업종)별 시나리오

### 🍔 햄버거
- 카테고리: 버거 / 사이드 / 음료  
- 옵션: 단품/세트, 사이즈, 얼음/빨대  
- 미션 예시: **"새우버거 세트 1개, 콜라 라지 1잔"**

### ☕ 카페
- 카테고리: 커피 / 차 / 디저트  
- 옵션: HOT/ICE, 샷추가, 당도, 얼음량, 테이크아웃  
- 미션 예시: **"아메리카노 ICE 톨 2잔, 베이글 1개(크림치즈)"**

### 🎬 영화관
- 단계: 영화 선택 → 시간/좌석 → 팝콘/음료 → 결제  
- 옵션: 2D/3D, 좌석 구역, 팝콘 사이즈  
- 미션 예시: **"오늘 19:10 '국민영화' 2매, 팝콘M, 콜라 1잔"**

### 🍱 식당
- 카테고리: 백반 / 찌개 / 면  
- 옵션: 맵기, 공기밥, 반찬 추가  
- 미션 예시: **"김치찌개 2(맵1 순1), 공기밥 1 추가"**

---

## 🧩 컴포넌트 사양서

### 1️⃣ `App.tsx`
- 전체 상태/모드 관리  
```ts
type AppMode = 'menu' | 'practice' | 'real';
type Domain = 'burger' | 'cafe' | 'cinema' | 'restaurant';
mode, domain, helpOpen, historyOpen 상태 포함

모드 전환 및 조건부 렌더

2️⃣ MainMenu.tsx
Props	설명
onSelectDomain(domain)	업종 선택
onSelectMode(mode)	연습 / 실전 모드 선택
onOpenHelp()	도움말 다이얼로그 열기
onOpenHistory()	학습 기록 보기

업종 카드 (아이콘 + 설명)

모드 선택 버튼

기록 / 도움말 버튼

3️⃣ KioskSimulator.tsx
Props	설명
domain	현재 업종
isPracticeMode	연습모드 여부
onExit()	홈으로 돌아가기

내부에서 업종별 컴포넌트 로드

/domains/Burger/, /domains/Cafe/, /domains/Cinema/, /domains/Restaurant/

4️⃣ 업종별 하위 컴포넌트 예시
Burger

BurgerCategoryTabs.tsx

BurgerMenuList.tsx

BurgerCart.tsx

BurgerCheckout.tsx

5️⃣ LearningHistory.tsx
localStorage 기반 기록 / 통계

최대 50건, 최신순

성공률, 시도 횟수, 최근 미션 표시

6️⃣ HelpDialog.tsx
사용법 / 팁 / FAQ

업종별 차이와 조작 방법 설명

색상 테마: blue-600

🗃 데이터 구조
ts
코드 복사
interface MenuItem {
  id: string;
  name: string;
  price: number;
  category: string;
  options?: Record<string, string | number | boolean>;
}

interface CartItem extends MenuItem {
  quantity: number;
}

interface Mission {
  domain: Domain;
  text: string;
  required: { name: string; quantity: number }[];
}

interface HistoryRecord {
  id: string;
  date: string;
  mission: string;
  domain: Domain;
  success: boolean;
  userOrder: { name: string; quantity: number }[];
  timestamp: number;
}
localStorage 키: kioskLearningHistory

정책: 최신순으로 최대 50건 저장

🔁 업무 흐름 & 모드 로직
🧭 연습 모드
시작 → 카테고리 → 메뉴 → 장바구니 → 결제

각 단계에서 버튼에 애니메이션 가이드

tsx
코드 복사
<motion.button
  animate={{ scale: [1, 1.05, 1] }}
  transition={{ repeat: Infinity, duration: 1.5 }}
/>
🎯 실전 모드
업종별 랜덤 미션 생성

checkMission() 으로 결과 판정

saveToHistory() 로 저장

성공/실패 화면 표시

🎨 디자인 시스템 & 접근성
항목	가이드
폰트	text-xl, text-2xl 중심
버튼 크기	최소 h-14, 중요 버튼 h-16
색상	연습: 파랑 / 실전: 초록 / 업종별 테마
피드백	hover, active, disabled 명확
모션	fade, slide, pulse (단순 효과만)
접근성	aria-label, 포커스 이동, 고대비 색상

🧪 테스트 시나리오
공통
업종 선택 → 연습 모드 단계별 진행

실전 모드 → 랜덤 미션 주문 → 성공 확인

잘못된 주문 → 실패 기록 저장

학습 기록 통계 확인

50건 초과 시 오래된 기록 삭제

업종별 포인트
햄버거: 세트/사이즈/빨대/얼음 옵션

카페: ICE/HOT, 샷추가, 당도 조절

영화관: 시간표, 좌석, 팝콘

식당: 맵기/공기밥 추가

⚙️ 설치 & 실행
bash
코드 복사
# 1) 설치
npm install

# 2) 개발 서버 실행
npm run dev

# 3) 빌드
npm run build
npm run preview
권장 환경

Node 18+

최신 Chrome 또는 Android WebView

배포: Vercel / Netlify / GitHub Pages (PWA 권장)

🔄 개선 프롬프트 예시
기능 개선
음성 안내: Web Speech API로 각 단계 안내 읽어주기

다국어 지원: 한국어/영어/중국어/일본어

난이도 추가: 쉬움(1~2), 보통(2~3), 어려움(3~5)

UX 개선
다크모드 지원

애니메이션 ON/OFF 설정

키보드 접근성 강화

데이터 관리
진행상황 저장(마지막 모드/단계 복원)

기록 내보내기(CSV/인쇄용)

추가 모드
시험 모드: 연속 5개 미션, 제한시간, 점수

자유 주문 모드: 미션 없이 자유 체험

🚀 로드맵
버전	주요 내용
v1.0	4개 업종 기본 시나리오 + 연습/실전 + 기록
v1.1	음성 안내 / 난이도 / 다크모드
v1.2	시험 모드 / 데이터 내보내기
v2.0	PWA 배포 (스플래시 / 아이콘 / 오프라인 캐시)
v2.1	Android 패키징 (Expo 또는 Capacitor)

📌 프로젝트 맥락
본 애플리케이션은 노인 대상 키오스크 안내 앱으로서
햄버거, 카페, 영화관, 식당 등 실제 환경의 시나리오를 기반으로
노인 사용자에게 실제 키오스크 조작 연습과 단계별 학습 경험을 제공합니다.
연습 모드와 실전 모드를 구분하여 학습의 부담을 줄이고,
기록/피드백 기능으로 학습 성취를 확인할 수 있습니다.
