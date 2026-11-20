import Link from "next/link";
import { Terminal } from "lucide-react";

const PARSERS = ["Spring Boot 3.x", "Jakarta EE 10", "Micronaut 4.x", "Quarkus 3"] as const;

export default function Hero() {
  return (
    <section className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-xl md:py-space-2xl">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-gutter-desktop items-center">
        {/* Coluna esquerda */}
        <div className="lg:col-span-6 flex flex-col items-start gap-space-lg">
          {/* Badge */}
          <div className="inline-flex items-center gap-space-xs px-space-sm py-1 rounded bg-surface-container-low shadow-sm">
            <span className="w-2 h-2 rounded-full bg-secondary-fixed-dim" />
            <span className="font-label-mono text-label-mono text-on-surface uppercase tracking-wider">
              AI-Powered API Documentation
            </span>
          </div>

          {/* Título */}
          <h1 className="font-headline-xl text-headline-xl-mobile md:text-headline-xl text-primary tracking-tight">
            Documentation shouldn&apos;t slow down development.
          </h1>

          {/* Texto de apoio */}
          <p className="font-body-lg text-body-lg text-on-surface-variant max-w-xl">
            Turn your Java repositories into clear, structured API documentation with the
            power of static AST intelligence and specialized LLMs.
          </p>

          {/* Botões */}
          <div className="flex flex-wrap items-center gap-space-md pt-space-xs">
            <Link
              href="/app-generator"
              className="group inline-flex items-center gap-space-sm bg-primary text-on-primary font-label-md text-label-md px-space-lg py-space-md rounded-lg shadow-sm hover:bg-primary-container transition-all"
            >
              <span>Get Started</span>
              <span className="w-2 h-2 rounded-full bg-secondary-fixed transition-transform group-hover:scale-125" />
            </Link>

            <Link
              href="/github"
              className="inline-flex items-center gap-space-sm bg-surface-container-lowest text-primary font-label-md text-label-md px-space-lg py-space-md rounded-lg shadow-sm hover:bg-surface-container-low transition-colors"
            >
              <Terminal size={18} aria-hidden="true" />
              <span>View on GitHub</span>
            </Link>
          </div>

          {/* Parsers suportados */}
          <div className="w-full pt-space-md flex flex-col gap-space-xs">
            <span className="font-label-mono text-[11px] uppercase tracking-wider text-on-surface-variant">
              Production-ready parsers
            </span>
            <ul className="flex flex-wrap items-center gap-space-xs">
              {PARSERS.map((parser) => (
                <li
                  key={parser}
                  className="px-space-sm py-0.5 rounded bg-surface-container-low font-label-mono text-code-sm text-on-surface"
                >
                  {parser}
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Coluna direita: placeholder */}
        <div className="lg:col-span-6 w-full">
          <div
            className="w-full aspect-4/3 rounded-xl bg-primary-container shadow-xl"
            aria-hidden="true"
          />
        </div>
      </div>
    </section>
  );
}