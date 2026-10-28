import type { Dictionary } from "@/i18n/dictionaries";

/** Labels are keys into the dictionary's `nav` section, so the tree is language-neutral. */
type NavLabel = keyof Dictionary["nav"];

export type NavLeaf = {
  href: string;
  label: NavLabel;
  /** match only this exact path, for a module's index page that prefixes every sibling */
  exact?: boolean;
};

export type NavModule = {
  key: string;
  label: NavLabel;
  /** every page of the module lives under this prefix */
  basePath: string;
  children: readonly NavLeaf[];
};

/** The sidebar tree: one entry per ERP module, each with its own pages. */
export const MODULES: readonly NavModule[] = [
  {
    key: "expenses",
    label: "expenses",
    basePath: "/expenses",
    children: [
      { href: "/expenses", label: "overview", exact: true },
      { href: "/expenses/list", label: "expenseList" },
      { href: "/expenses/disbursements", label: "advances" },
      { href: "/expenses/budgets", label: "budgets" },
    ],
  },
  {
    key: "inventory",
    label: "inventory",
    basePath: "/inventory",
    children: [
      { href: "/inventory", label: "stock", exact: true },
      { href: "/inventory/movements", label: "movements" },
      { href: "/inventory/locations", label: "locations" },
      { href: "/inventory/other-use", label: "otherUse" },
    ],
  },
  {
    key: "people",
    label: "people",
    basePath: "/people",
    children: [{ href: "/people", label: "employees", exact: true }],
  },
  {
    key: "help",
    label: "help",
    basePath: "/help",
    children: [
      { href: "/help", label: "helpStart", exact: true },
      { href: "/help/expenses", label: "helpExpenses" },
      { href: "/help/inventory", label: "helpInventory" },
      { href: "/help/people", label: "helpPeople" },
      { href: "/help/glossary", label: "helpGlossary" },
    ],
  },
];

function isUnder(pathname: string, prefix: string) {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

export function isLeafActive(pathname: string, leaf: NavLeaf) {
  return leaf.exact ? pathname === leaf.href : isUnder(pathname, leaf.href);
}

export function isModuleActive(pathname: string, module: NavModule) {
  return isUnder(pathname, module.basePath);
}
