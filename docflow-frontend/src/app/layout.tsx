import type { Metadata } from "next";
import { Geist, Geist_Mono, Inter, JetBrains_Mono} from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

const inter = Inter({ variable: "--font-inter", subsets: ["latin"] });
const jetbrains = JetBrains_Mono({ variable: "--font-jetbrains", subsets: ["latin"] });


export const metadata: Metadata = {
  title: "DocFlow",
  description: "Documentação de APIs Java gerada com AST e LLM.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="en"
      className={`${geistMono.variable} ${inter.variable} ${jetbrains.variable}`}
    >
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
