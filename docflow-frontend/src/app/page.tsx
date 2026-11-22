import CallToAction from "@/components/CTA";
import Hero from "@/components/Hero";
import HowItWorks from "@/components/HowItWorks";

export default function Home() {
  return <>
    <main className="w-full pt-16 bg-surface-container-lowest min-h-screen">
      <Hero />
      <HowItWorks/>
      <CallToAction />
    </main>
  </>
  
}
