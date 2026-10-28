import type { GlossaryGroup } from "../glossary/GlossaryTag";

/** The glossary is data: the app draws each tag itself; each language only supplies the words. */
export interface GlossaryContent {
  eyebrow: string;
  title: string;
  lead: string;
  sections: {
    id: string;
    title: string;
    intro?: string;
    group: GlossaryGroup;
    entries: { value: string; meaning: string }[];
  }[];
  termsTitle: string;
  terms: { term: string; meaning: string }[];
}
