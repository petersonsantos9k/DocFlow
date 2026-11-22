'use client'
import { ArrowUp } from "lucide-react";
import { motion } from "motion/react";

export default function Footer() {
  const year = new Date().getFullYear();

  return (


    <motion.footer
      className="w-full bg-surface-container-low"
      initial={{ opacity: 0, y: 15 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, amount: 0.3 }}
      transition={{ duration: 0.6, ease: "easeOut" }}
    >
      <div className="max-w-7xl mx-auto px-margin md:px-margin-desktop py-space-lg border-t border-surface-variant pt-space-lg flex flex-col sm:flex-row items-start sm:items-center justify-between gap-space-md">
        <p className="font-body-sm text-body-sm text-on-surface-variant">
          © DocFlow · {year}
        </p>

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
    </motion.footer>


  );
}