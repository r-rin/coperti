import { getLocale } from "@/i18n/server";

import { glossaryEn } from "../content/glossary.en";
import type { GlossaryContent } from "../content/glossary.types";
import { glossaryUk } from "../content/glossary.uk";
import { Guide, GuideHero, Section, Terms } from "../guide/Guide";
import { BookOutlined, TagsOutlined } from "../guide/icons";
import { GlossaryTag } from "./GlossaryTag";

const CONTENT: Record<string, GlossaryContent> = { en: glossaryEn, uk: glossaryUk };

export default async function HelpGlossaryPage() {
  const content = CONTENT[await getLocale()];

  return (
    <Guide>
      <GuideHero icon={<BookOutlined />} eyebrow={content.eyebrow} title={content.title} lead={content.lead} />

      {content.sections.map((section) => (
        <Section key={section.id} id={section.id} icon={<TagsOutlined />} title={section.title}>
          {section.intro && <p>{section.intro}</p>}
          <Terms
            items={section.entries.map((entry) => ({
              key: entry.value,
              term: <GlossaryTag group={section.group} value={entry.value} />,
              meaning: entry.meaning,
            }))}
          />
        </Section>
      ))}

      <Section id="terms" icon={<BookOutlined />} title={content.termsTitle}>
        <Terms
          items={content.terms.map((term) => ({ key: term.term, term: <strong>{term.term}</strong>, meaning: term.meaning }))}
        />
      </Section>
    </Guide>
  );
}
