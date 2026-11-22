import Link from "next/link";
import { ArrowRight, Code } from "lucide-react";


export default function CallToAction() {
  return (
    <section className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-2xl">
      <div className="w-full bg-primary rounded-xl p-space-xl md:p-space-2xl shadow-xl flex flex-col items-center text-center gap-space-lg">
        {/* Text */}
        <div className="flex flex-col items-center gap-space-xs max-w-2xl">
          <span className="font-label-mono text-code-sm text-secondary-fixed uppercase tracking-wider font-semibold">
            Get Started with DocFlow
          </span>

          <h2 className="font-headline-xl text-headline-xl-mobile md:text-headline-xl text-on-primary tracking-tight">
            Your API documentation starts with your code.
          </h2>

          <p className="font-body-lg text-body-lg text-tertiary-fixed-dim max-w-xl">
            Connect your Java repository and let DocFlow analyze your source code
            to generate clear, structured API documentation.
          </p>
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row items-center gap-space-md">
          <Link
            href="/app-generator"
            className="inline-flex items-center justify-center gap-space-sm bg-secondary-container text-on-secondary-fixed font-label-md text-label-md px-space-xl py-space-md rounded-lg shadow-sm hover:bg-secondary-fixed-dim transition-all"
          >
            <span>Start Generating</span>
            <ArrowRight size={18} aria-hidden="true" />
          </Link>

          <Link
            href="https://github.com/PetersonSantos9k/DocFlow"
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center gap-space-sm bg-inverse-surface text-on-primary font-label-md text-label-md px-space-xl py-space-md rounded-lg shadow-sm hover:bg-primary-container transition-colors"
          >
            <Code size={18} aria-hidden="true" />
            <span>Explore on GitHub</span>
          </Link>
        </div>
      </div>
    </section>
  );
}
