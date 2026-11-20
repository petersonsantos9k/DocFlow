import Link from "next/link";
import { ArrowRight, Code } from "lucide-react";

export default function CallToAction() {
  return (
    <section className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-2xl">
      <div className="w-full bg-primary rounded-xl p-space-xl md:p-space-2xl shadow-xl flex flex-col items-center text-center gap-space-lg">
        {/* Texto */}
        <div className="flex flex-col items-center gap-space-xs max-w-2xl">
          <span className="font-label-mono text-code-sm text-secondary-fixed uppercase tracking-wider font-semibold">
            Immediate Generation
          </span>
          <h2 className="font-headline-xl text-headline-xl-mobile md:text-headline-xl text-on-primary tracking-tight">
            Make your API documentation effortless.
          </h2>
          <p className="font-body-lg text-body-lg text-tertiary-fixed-dim max-w-xl">
            Stop manually maintaining stale swagger annotations and out-of-sync markdown files.
            Connect your Java repo and generate clean docs in seconds.
          </p>
        </div>

        {/* Botões */}
        <div className="flex flex-col sm:flex-row items-center gap-space-md">
          <Link
            href="/app-generator"
            className="inline-flex items-center justify-center gap-space-sm bg-secondary-container text-on-secondary-fixed font-label-md text-label-md px-space-xl py-space-md rounded-lg shadow-sm hover:bg-secondary-fixed-dim transition-all"
          >
            <span>Start Generating</span>
            <ArrowRight size={18} aria-hidden="true" />
          </Link>

          <Link
            href="/github"
            className="inline-flex items-center justify-center gap-space-sm bg-inverse-surface text-on-primary font-label-md text-label-md px-space-xl py-space-md rounded-lg shadow-sm hover:bg-primary-container transition-colors"
          >
            <Code size={18} aria-hidden="true" />
            <span>Explore on GitHub</span>
          </Link>
        </div>

        {/* Linha de rodapé */}
        <div className="flex items-center gap-space-sm pt-space-xs">
          <span className="w-1.5 h-1.5 rounded-full bg-secondary-fixed" />
          <span className="font-label-mono text-[11px] text-tertiary-fixed-dim">
            No credit card required • Open source core • Zero configuration
          </span>
        </div>
      </div>
    </section>
  );
}