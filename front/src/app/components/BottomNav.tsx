"use client";

import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { Home, Search, MessageSquare, User } from "lucide-react";

export default function BottomNav() {
  const pathname = usePathname();
  const router = useRouter();

  if (pathname === "/login" || pathname === "/signup") return null;

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
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100">
      <div className="grid grid-cols-4">
        <Link
          href="/"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/") ? "text-blue-600" : "text-gray-400"
          }`}
        >
          <Home size={20} />
          홈
        </Link>

        <button
          type="button"
          onClick={handleSearchClick}
          className="flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium text-gray-400 transition"
        >
          <Search size={20} />
          검색
        </button>

        <Link
          href="/board"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/board") ? "text-blue-600" : "text-gray-400"
          }`}
        >
          <MessageSquare size={20} />
          게시판
        </Link>

        <Link
          href="/mypage"
          className={`flex flex-col items-center justify-center gap-0.5 py-2.5 text-xs font-medium transition ${
            isActive("/mypage") ? "text-blue-600" : "text-gray-400"
          }`}
        >
          <User size={20} />
          마이페이지
        </Link>
      </div>
    </nav>
  );
}
