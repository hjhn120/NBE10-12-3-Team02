"use client";

import { Check, Share2 } from "lucide-react";
import { useState } from "react";

export default function ShareButton({
  url,
  className = "",
}: {
  url?: string;
  className?: string;
}) {
  const [copied, setCopied] = useState(false);

  const handleClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    const target = url
      ? url.startsWith("http")
        ? url
        : window.location.origin + url
      : window.location.href;
    try {
      await navigator.clipboard.writeText(target);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } catch {
      /* 클립보드 API 미지원 환경에서 조용히 무시 */
    }
  };

  return (
    <button
      onClick={handleClick}
      aria-label="링크 복사"
      className={`flex items-center gap-1.5 px-2.5 py-1.5 rounded-lg border text-sm font-medium transition ${
        copied
          ? "bg-green-50 dark:bg-green-900/40 border-green-200 dark:border-green-800 text-green-600 dark:text-green-400"
          : "bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700 text-gray-500 dark:text-gray-400 hover:border-blue-300 dark:hover:border-blue-700 hover:text-blue-600 dark:hover:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-950/40"
      } ${className}`}
    >
      {copied ? (
        <>
          <Check size={16} className="shrink-0" />
          <span>복사됨</span>
        </>
      ) : (
        <Share2 size={16} />
      )}
    </button>
  );
}
