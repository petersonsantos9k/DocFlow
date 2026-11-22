"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Menu, X } from "lucide-react";
import { motion } from "motion/react";

const NAV_LINKS = [
  { label: "How it Works", href: "#how-it-works" },
  { label: "GitHub", href: "#github" },
] as const;

export default function Header() {
  const [open, setOpen] = useState(false);
  const [user] = useState<{ name: string } | null>(null);
  const pathname = usePathname();

  useEffect(() => {
    setOpen(false);
  }, [pathname]);

  useEffect(() => {
    if (!open) return;

    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === "Escape") setOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    document.body.style.overflow = "hidden";

    return () => {
      document.removeEventListener("keydown", onKeyDown);
      document.body.style.overflow = "";
    };
  }, [open]);

  const linkClass = (href: string) =>
    pathname === href
      ? "text-primary font-medium"
      : "text-on-surface-variant hover:text-on-surface";

  const initials = user
    ? user.name
      .split(" ")
      .filter(Boolean)
      .slice(0, 2)
      .map((part: string) => part[0])
      .join("")
      .toUpperCase()
    : "";


  return (
    <motion.header
      initial={{ opacity: 0, y: -12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, ease: "easeOut" }}
      className="fixed top-0 left-0 right-0 z-50 bg-surface-container-lowest border-b border-surface-variant"
    >
      <div className="h-16 max-w-7xl mx-auto px-margin md:px-margin-desktop flex items-center justify-between">
        <div className="flex items-center gap-space-md ">
          <Link href="/" className="font-title-md text-title-md text-primary tracking-tight font-bold">
            DocFlow
          </Link>
          <span className="hidden sm:inline-flex items-center px-space-xs py-0.5 rounded bg-surface-container-low text-on-surface-variant font-label-mono text-label-mono border border-surface-variant">
            v1.0.0
          </span>
        </div>

        <nav className="hidden md:flex items-center gap-space-lg">
          {NAV_LINKS.map(({ label, href }) => (
            <Link
              key={href}
              href={href}
              className={`group font-body-md text-body-md transition-colors ${linkClass(href)}`}
            >
              <span className="relative inline-block">
                {label}
                <span
                  aria-hidden="true"
                  className="absolute -bottom-1 left-0 h-0-5 w-full origin-left scale-x-0 bg-primary transition-transform duration-300 group-hover:scale-x-100 group-focus-visible:scale-x-100"
                />
              </span>
            </Link>
          ))}
        </nav>

        <div className="flex items-center gap-space-md">

          {user ? (
            <Link
              href="/app-generator"
              aria-label="Minha conta"
              className="w-8 h-8 shrink-0 rounded-full bg-secondary-container text-on-secondary-fixed font-label-mono text-code-sm font-semibold flex items-center justify-center hover:bg-secondary-fixed-dim transition-colors"
            >
              {initials}
            </Link>
          ) : (
            <>
              <Link
                href="/login"
                className="hidden sm:inline-flex px-space-sm py-space-sm font-label-md text-label-md text-on-surface-variant hover:text-primary transition-colors"
              >
                Log in
              </Link>
              <Link
                href="/signup"
                className="inline-flex shrink-0 items-center justify-center bg-primary text-on-primary font-label-md text-label-md px-space-md py-space-sm rounded-lg hover:bg-primary-container transition-colors"
              >
                Sign up
              </Link>
            </>
          )}

          <button
            type="button"
            className="md:hidden inline-flex items-center justify-center w-9 h-9 rounded-lg text-primary hover:bg-surface-container-low transition-colors cursor-pointer"
            aria-label={open ? "Fechar menu" : "Abrir menu"}
            aria-expanded={open}
            aria-controls="mobile-menu"
            onClick={() => setOpen((v) => !v)}
          >
            {open ? <X size={24} aria-hidden="true" /> : <Menu size={24} aria-hidden="true" />}
          </button>
        </div>
      </div>

      {open && (
        <div
          className="md:hidden fixed inset-0 top-16 bg-black/20"
          onClick={() => setOpen(false)}
          aria-hidden="true"
        />
      )}

      <nav
        id="mobile-menu"
        className={`md:hidden absolute top-16 left-0 right-0 bg-surface-container-lowest border-b border-surface-variant shadow-lg px-margin py-space-sm flex flex-col transition-all duration-200 origin-top ${open
          ? "opacity-100 scale-y-100 pointer-events-auto"
          : "opacity-0 scale-y-95 pointer-events-none"
          }`}
      >
        {NAV_LINKS.map(({ label, href }) => (
          <Link
            key={href}
            href={href}
            tabIndex={open ? 0 : -1}
            className={`py-space-md border-b border-surface-variant last:border-b-0 font-body-lg text-body-lg transition-colors ${linkClass(href)}`}
          >
            {label}
          </Link>
        ))}
      </nav>
    </motion.header>
  );
}