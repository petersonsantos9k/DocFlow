import type { ReactNode } from "react";
import { BookOpen, FolderGit2, Network, type LucideIcon } from "lucide-react";

type Step = {
  number: string;
  title: string;
  description: string;
  icon: LucideIcon;
  footer: ReactNode;
};


const STEPS: Step[] = [
  {
    number: "01",
    title: "CONNECT",
    description:
      "Provide a GitHub repository URL. DocFlow retrieves the source code and prepares it for automated analysis.",
    icon: FolderGit2,
    footer: (
      <div className="p-space-sm bg-surface-container-low rounded-lg font-label-mono text-code-sm text-on-surface">
        <code>github.com/user/repository</code>
      </div>
    ),
  },
  {
    number: "02",
    title: "ANALYZE",
    description:
      "DocFlow analyzes Java source code using AST parsing to identify API controllers, endpoints, HTTP methods, and DTO structures.",
    icon: Network,
    footer: (
      <div className="p-space-sm bg-surface-container-low rounded-lg flex items-center justify-center">
        <svg
          className="w-full h-8 text-primary"
          viewBox="0 0 240 32"
          fill="none"
          aria-hidden="true"
        >
          <rect x="5" y="8" width="40" height="16" rx="2" fill="currentColor" fillOpacity="0.1" />
          <text x="25" y="20" textAnchor="middle" fontFamily="monospace" fontSize="9" fill="currentColor">
            AST
          </text>
          <path d="M45 16 L65 16" stroke="currentColor" strokeDasharray="2 2" />
          <rect x="65" y="8" width="75" height="16" rx="2" fill="currentColor" fillOpacity="0.1" />
          <text x="102" y="20" textAnchor="middle" fontFamily="monospace" fontSize="9" fill="currentColor">
            Controllers
          </text>
          <path d="M140 16 L160 16" stroke="currentColor" strokeDasharray="2 2" />
          <rect x="160" y="8" width="75" height="16" rx="2" fill="currentColor" fillOpacity="0.1" />
          <text x="197" y="20" textAnchor="middle" fontFamily="monospace" fontSize="9" fill="currentColor">
            DTOs
          </text>
        </svg>
      </div>
    ),
  },
  {
    number: "03",
    title: "GENERATE",
    description:
      "AI transforms the extracted API information into clear, structured documentation that can be viewed and exported as Markdown.",
    icon: BookOpen,
    footer: (
      <div className="flex items-center gap-space-xs">
        <span className="px-space-sm py-1 rounded bg-secondary-container text-on-secondary-fixed font-label-mono text-[11px] font-semibold">
          Markdown
        </span>
      </div>
    ),
  },
];


export default function HowItWorks() {
  return (
    <section id="how-it-works" className="w-full bg-surface-container-low py-space-2xl">
      <div className="max-w-7xl mx-auto px-margin md:px-margin-desktop flex flex-col gap-space-xl">
        <div className="flex flex-col items-start gap-space-xs max-w-2xl">
          <span className="font-label-mono text-label-mono text-secondary font-bold uppercase tracking-wider">
            AUTOMATED DOCUMENTATION PIPELINE
          </span>
          <h2 className="font-headline-lg-mobile md:font-headline-lg text-headline-lg-mobile md:text-headline-lg text-primary tracking-tight">
            Three steps from repo to readable docs
          </h2>
          <p className="font-body-md text-body-md text-on-surface-variant">
            Connect your GitHub repository and let DocFlow analyze your source code to generate structured API documentation — without modifying your project.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-gutter">
          {STEPS.map(({ number, title, description, icon: Icon, footer }) => (
            <div
              key={number}
              className="bg-surface-container-lowest rounded-xl p-space-xl flex flex-col justify-between gap-space-lg shadow-sm hover:shadow-md transition-shadow"
            >
              <div className="flex flex-col gap-space-md">
                <div className="flex items-center justify-between">
                  <span className="font-label-mono text-code-md font-bold text-secondary">
                    {number}
                  </span>
                  <Icon size={24} className="text-primary" aria-hidden="true" />
                </div>
                <div className="flex flex-col gap-space-xs">
                  <h3 className="font-title-md text-title-md text-primary font-bold">{title}</h3>
                  <p className="font-body-md text-body-md text-on-surface-variant">{description}</p>
                </div>
              </div>
              {footer}
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}