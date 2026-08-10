"use client";

import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { Home, Search, MessageSquare, User } from "lucide-react";
import { useSyncExternalStore } from "react";

const MOBILE_QUERY = "(max-width: 767px)";

function subscribe(callback: () => void) {
  const mql = window.matchMedia(MOBILE_QUERY);
  mql.addEventListener("change", callback);
  return () => mql.removeEventListener("change", callback);
}

function getSnapshot() {
  return window.matchMedia(MOBILE_QUERY).matches;
}

// 서버는 실제 뷰포트를 알 수 없으니 항상 "모바일 아님"으로 간주해 null을 렌더링한다.
// 예전에는 md:hidden으로 CSS만 숨겼는데, 서버 렌더링 HTML엔 뷰포트와 무관하게 항상
// BottomNav 마크업이 포함되고 하이드레이션이 끝나야 클라이언트가 실제 뷰포트를 보고
// desktop이면 걷어냈다. 그 사이 짧은 창(특히 콜드 스타트인 CI에서 더 큼) 동안은 데스크톱
// 에서도 BottomNav가 DOM에 남아있어서, 데스크톱 뷰포트로 도는 E2E 테스트의 범용 locator
// (h1/h2/h3/svg/button 등)가 페이지 콘텐츠보다 이 버튼/아이콘을 먼저 집어버렸다.
// useSyncExternalStore로 서버/클라이언트 초기 렌더 결과를 아예 일치시켜 이 불일치를 없앤다.
function getServerSnapshot() {
  return false;
}

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();
  const isMobile = useSyncExternalStore(
    subscribe,
    getSnapshot,
    getServerSnapshot,
  );

  if (pathname === "/login" || pathname === "/signup") return null;
  if (!isMobile) return null;

  const isActive = (href: string) =>
    href === "/" ? pathname === "/" : pathname.startsWith(href);

  const handleSearchClick = () => {
    if (pathname === "/") {
      const el = document.getElementById("concert-search");
      el?.scrollIntoView({ behavior: "smooth", block: "center" });
      el?.focus();
    } else {
      router.push("/?focus=search");
    }
  };

  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white dark:bg-gray-900 border-t border-gray-100 dark:border-gray-800">
      <div className="grid grid-cols-4">
        <Link
          href="/"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/") ? "text-blue-600 dark:text-blue-400" : "text-gray-400 dark:text-gray-500"
          }`}
        >
          <Home size={20} />
          홈
        </Link>

        <button
          type="button"
          onClick={handleSearchClick}
          className="flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium text-gray-400 dark:text-gray-500 transition"
        >
          <Search size={20} />
          검색
        </button>

        <Link
          href="/board"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/board") ? "text-blue-600 dark:text-blue-400" : "text-gray-400 dark:text-gray-500"
          }`}
        >
          <MessageSquare size={20} />
          게시판
        </Link>

        <Link
          href="/mypage"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/mypage") ? "text-blue-600 dark:text-blue-400" : "text-gray-400 dark:text-gray-500"
          }`}
        >
          <User size={20} />
          마이페이지
        </Link>
      </div>
    </nav>
  );
}
