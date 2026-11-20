import Link from "next/link";

const FOOTER_LINKS = [
  { label: "Architecture", href: "/features" },
  { label: "CLI & SDK", href: "/how-it-works" },
  { label: "Open Source", href: "/github" },
  { label: "Developer Portal", href: "/login" },
] as const;

export default function Footer() {
  const year = new Date().getFullYear();

  return (
    <footer className="w-full bg-surface-container-low border-t border-surface-variant">
      <div className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-2xl">
        <div className="flex flex-col md:flex-row items-start md:items-center justify-between gap-space-xl">
          {/* Status + copyright */}
          <div className="flex flex-col gap-space-xs">
            <div className="flex items-center gap-space-sm">
              <span className="w-2 h-2 rounded-full bg-secondary-fixed-dim ring-2 ring-secondary/20" />
              <span className="font-label-mono text-label-mono text-on-surface">
                System operational • All models online
              </span>
            </div>
            <p className="font-body-sm text-body-sm text-on-surface-variant">
              © {year} DocFlow Inc. Engineered for developer rigor and zero-latency specs.
            </p>
          </div>

          {/* Links */}
          <nav aria-label="Footer" className="flex flex-wrap items-center gap-space-lg">
            {FOOTER_LINKS.map(({ label, href }) => (
              <Link
                key={href}
                href={href}
                className="font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-colors"
              >
                {label}
              </Link>
            ))}
          </nav>
        </div>
      </div>
    </footer>
  );
}