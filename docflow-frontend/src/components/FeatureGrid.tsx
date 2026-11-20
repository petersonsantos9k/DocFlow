import { Braces, Brain, FileText, Share2, type LucideIcon } from "lucide-react";

type Feature = {
  title: string;
  description: string;
  icon: LucideIcon;
  tag: string;
  tagVariant: "dark" | "light";
};

const FEATURES: Feature[] = [
  {
    title: "GitHub Repository Integration",
    description:
      "Seamlessly read any public repository with explicit branch, release tag, or commit hash targeting. No write permissions required.",
    icon: Share2,
    tag: "GET /repos/{owner}/{repo}/git/trees",
    tagVariant: "dark",
  },
  {
    title: "Automatic API Extraction",
    description:
      "Deep static analysis parses controllers, routes, request bodies, and validation rules without running server code or provisioning runtimes.",
    icon: Braces,
    tag: "JavaParser + Spoon AST engine",
    tagVariant: "light",
  },
  {
    title: "AI-Powered Documentation",
    description:
      "Context-aware models fill in missing details, explain ambiguous error conditions, and generate production-ready payload mocks automatically.",
    icon: Brain,
    tag: "Spec-tuned developer LLM",
    tagVariant: "light",
  },
  {
    title: "Markdown & OpenAPI Output",
    description:
      "Export battle-tested OpenAPI 3.1 YAML or standard Markdown formatted directly for READMEs, Docusaurus, Mintlify, and Nextra.",
    icon: FileText,
    tag: "Instant .md & .json download",
    tagVariant: "light",
  },
];

const TAG_STYLES: Record<Feature["tagVariant"], string> = {
  dark: "text-secondary-container bg-primary",
  light: "text-on-surface bg-surface-container-low",
};

export default function FeatureGrid() {
  return (
    <section id="features" className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-2xl">
      <div className="flex flex-col gap-space-xl">
        {/* Cabeçalho */}
        <div className="flex flex-col md:flex-row md:items-end justify-between gap-space-md">
          <div className="flex flex-col gap-space-xs">
            <span className="font-label-mono text-label-mono text-secondary font-bold uppercase tracking-wider">
              Engineered for Rigor
            </span>
            <h2 className="font-headline-lg-mobile md:font-headline-lg text-headline-lg-mobile md:text-headline-lg text-primary tracking-tight">
              Built specifically for backend teams
            </h2>
          </div>
          <p className="font-body-md text-body-md text-on-surface-variant max-w-md">
            Eliminates documentation drift permanently. Stay in code without maintaining
            separate swagger schemas.
          </p>
        </div>

        {/* Grade 2x2 */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-gutter">
          {FEATURES.map(({ title, description, icon: Icon, tag, tagVariant }) => (
            <div
              key={title}
              className="bg-surface-container-lowest p-space-xl rounded-xl shadow-sm flex flex-col gap-space-md"
            >
              <div className="w-10 h-10 rounded-lg bg-surface-container-low flex items-center justify-center text-primary">
                <Icon size={20} aria-hidden="true" />
              </div>

              <div className="flex flex-col gap-space-xs">
                <h3 className="font-title-md text-title-md text-primary font-bold">{title}</h3>
                <p className="font-body-md text-body-md text-on-surface-variant">{description}</p>
              </div>

              <div className="mt-auto pt-space-sm">
                <span
                  className={`inline-block px-space-sm py-1 rounded font-label-mono text-code-sm font-semibold ${TAG_STYLES[tagVariant]}`}
                >
                  {tag}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}