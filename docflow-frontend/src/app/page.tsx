import Benchmarks from "@/components/Benchmarks";
import CallToAction from "@/components/CallToAction";
import FeatureGrid from "@/components/FeatureGrid";
import Header from "@/components/Header";
import Hero from "@/components/Hero";
import HowItWorks from "@/components/HowItWorks";
import Image from "next/image";

export default function Home() {
  return <>
    <main className="w-full pt-16 bg-surface-container-lowest min-h-screen">
      <Hero />
      <HowItWorks/>
      <FeatureGrid/>
      <Benchmarks />
      <CallToAction />
    </main>
  </>
  
}
