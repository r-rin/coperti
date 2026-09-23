"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";

import { useI18n } from "@/i18n/I18nProvider";
import { isLeafActive, isModuleActive, MODULES } from "./modules";
import styles from "./shell.module.css";

export function Sidebar() {
  const pathname = usePathname();
  const { t } = useI18n();
  // only explicit toggles are stored; an untouched module follows the route,
  // so navigating into it opens it without an effect syncing state
  const [toggled, setToggled] = useState<Record<string, boolean>>({});

  return (
    <nav className={styles.nav} aria-label={t.shell.modules}>
      {MODULES.map((module) => {
        const current = isModuleActive(pathname, module);
        const open = toggled[module.key] ?? current;
        const listId = `nav-${module.key}`;

        return (
          <div key={module.key} className={styles.group}>
            <button
              type="button"
              className={`${styles.groupToggle} ${current ? styles.groupToggleCurrent : ""}`}
              aria-expanded={open}
              aria-controls={listId}
              onClick={() => setToggled((prev) => ({ ...prev, [module.key]: !open }))}
            >
              <span>{t.nav[module.label]}</span>
              <svg
                className={`${styles.chevron} ${open ? styles.chevronOpen : ""}`}
                viewBox="0 0 16 16"
                aria-hidden="true"
              >
                <path d="M6 4l4 4-4 4" fill="none" stroke="currentColor" strokeWidth="1.5" />
              </svg>
            </button>

            <ul id={listId} className={styles.branch} hidden={!open}>
              {module.children.map((leaf) => {
                const active = isLeafActive(pathname, leaf);
                return (
                  <li key={leaf.href}>
                    <Link
                      href={leaf.href}
                      className={`${styles.navLink} ${active ? styles.navLinkActive : ""}`}
                      aria-current={active ? "page" : undefined}
                    >
                      {t.nav[leaf.label]}
                    </Link>
                  </li>
                );
              })}
            </ul>
          </div>
        );
      })}
    </nav>
  );
}
