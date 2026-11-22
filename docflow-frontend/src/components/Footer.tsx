import { ArrowUp } from "lucide-react";
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

    
<footer className="w-full bg-surface-container-low">
  <div className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-lg">
    <div className="border-t border-surface-variant pt-space-lg">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-space-md">
        {/* Copyright */}
        <p className="font-body-sm text-body-sm text-on-surface-variant">
          © DocFlow · {year}
        </p>

        {/* Navigation */}
        <div className="flex items-center gap-space-lg">
          <a
            href="#top"
            className="flex items-center gap-space-xs font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-colors"
          >
            Back to top
            <ArrowUp size={14} />
          </a>

          <a
            href="https://github.com/PetersonSantos9k/DocFlow"
            target="_blank"
            rel="noopener noreferrer"
            className="font-body-sm text-body-sm text-on-surface-variant hover:text-on-surface transition-colors"
          >
            GitHub
          </a>
        </div>
      </div>
    </div>
  </div>
</footer>


  );
}