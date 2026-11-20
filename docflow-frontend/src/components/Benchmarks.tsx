type Stat = {
  value: string;
  label: string;
  accent?: boolean;
};

const STATS: Stat[] = [
  { value: "4.2s", label: "Avg extraction time" },
  { value: "100%", label: "Static analysis safety" },
  { value: "0", label: "Runtime dependencies" },
  { value: "OAS 3.1", label: "Compliant schema output", accent: true },
];

export default function Benchmarks() {
  return (
    <section className="w-full bg-surface-container-low py-space-2xl">
      <div className="max-w-7xl mx-auto px-margin md:px-margin-desktop">
        <div className="bg-surface-container-lowest rounded-xl p-space-xl md:p-space-2xl shadow-sm flex flex-col lg:flex-row items-center justify-between gap-space-xl">
          {/* Texto */}
          <div className="flex flex-col gap-space-sm max-w-lg">
            <div className="flex items-center gap-space-xs">
              <span className="w-2 h-2 rounded-full bg-secondary-fixed-dim" />
              <span className="font-label-mono text-code-sm text-secondary uppercase tracking-wider font-bold">
                Benchmarked Velocity
              </span>
            </div>
            <h3 className="font-headline-lg-mobile md:font-headline-lg text-headline-lg-mobile md:text-headline-lg text-primary tracking-tight">
              Zero-latency turnaround for 200+ endpoint monoliths
            </h3>
            <p className="font-body-md text-body-md text-on-surface-variant">
              Generate fully typed, schema-complete specifications faster than your unit test
              suite finishes compiling.
            </p>
          </div>

          {/* Métricas */}
          <div className="grid grid-cols-2 gap-space-md w-full lg:w-auto">
            {STATS.map(({ value, label, accent }) => (
              <div
                key={label}
                className="p-space-lg rounded-lg bg-surface-container-low flex flex-col gap-space-xs"
              >
                <span
                  className={`font-label-mono text-headline-sm font-bold ${
                    accent ? "text-secondary" : "text-primary"
                  }`}
                >
                  {value}
                </span>
                <span className="font-body-sm text-body-sm text-on-surface-variant">{label}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}