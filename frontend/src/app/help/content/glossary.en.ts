import type { GlossaryContent } from "./glossary.types";

export const glossaryEn: GlossaryContent = {
  eyebrow: "User guide · Glossary",
  title: "What every tag means",
  lead: "Each status and type below is shown exactly as it appears in the app, with what it means for you.",
  sections: [
    {
      id: "advance",
      title: "Advance status",
      intro: "Shown on Expenses → Advances.",
      group: "advance",
      entries: [
        { value: "OPEN", meaning: "The worker holds this cash. Receipts can still be paid from it." },
        {
          value: "CLOSED",
          meaning:
            "Settled. It closes by itself when fully used for receipts, or by hand when the worker returned or accounted for the rest. Nothing more can be paid from it.",
        },
        {
          value: "CANCELLED",
          meaning:
            "Recorded by mistake — the cash never left the till. Only possible while nothing was paid from it; it does not count against the float.",
        },
      ],
    },
    {
      id: "movement",
      title: "Movement types",
      intro: "Every line on Inventory → Movements has one. The first seven you record yourself; the rest are recorded by other parts of the system.",
      group: "movement",
      entries: [
        { value: "RECEIPT", meaning: "A delivery from a supplier into one of your locations." },
        { value: "ISSUE_TO_EMPLOYEE", meaning: "Stock handed to a person; it now counts as being with them." },
        { value: "RETURN_FROM_EMPLOYEE", meaning: "A person brought stock back to a location." },
        { value: "TRANSFER", meaning: "Stock moved between two of your locations." },
        {
          value: "QUALITY_CHANGE",
          meaning: "Same place, different quality — e.g. good parts found defective, or quarantined parts cleared.",
        },
        { value: "SCRAP", meaning: "Stock thrown away. It leaves inventory for good." },
        { value: "RETURN_TO_SUPPLIER", meaning: "Stock sent back to the supplier." },
        { value: "CONSUME", meaning: "Material used up while making something (recorded by work orders)." },
        { value: "PRODUCE", meaning: "Finished or semi-finished items coming out of production (recorded by work orders)." },
        { value: "OTHER_USE", meaning: "Stock taken by an approved other-use request: samples, maintenance, tools." },
        { value: "RECOVER_FROM_TEARDOWN", meaning: "Usable parts salvaged by taking apart a defective unit." },
      ],
    },
    {
      id: "quality",
      title: "Quality",
      intro: "Stock of the same item is counted separately for each quality. Only Good stock in warehouses is available for use.",
      group: "quality",
      entries: [
        { value: "GOOD", meaning: "Fit for use." },
        { value: "DEFECTIVE", meaning: "Known to be faulty. Kept apart until scrapped, returned or repaired." },
        { value: "QUARANTINED", meaning: "Held back until someone checks it — not yet known to be good or bad." },
      ],
    },
    {
      id: "location",
      title: "Location kinds",
      intro: "The kind says what a place is used for. Kinds marked “virtual” are bookkeeping places, not real shelves.",
      group: "location",
      entries: [
        { value: "WAREHOUSE", meaning: "A store. Only Good stock here counts as available." },
        { value: "WIP", meaning: "Work in progress: a bench or line where things are being made." },
        { value: "REPAIR", meaning: "Where items wait for or undergo repair." },
        { value: "QUARANTINE", meaning: "A separate area for stock that must be checked first." },
        { value: "FINISHED_GOODS", meaning: "Finished products ready to ship." },
        { value: "EMPLOYEE", meaning: "A person's own location — created automatically the first time stock is handed to them." },
        { value: "V_SUPPLIER", meaning: "Where deliveries come from and supplier returns go to." },
        { value: "V_CONSUMED", meaning: "Where material goes when it is used up in production." },
        { value: "V_SCRAP", meaning: "Where scrapped stock goes." },
        { value: "V_OTHER_USE", meaning: "Where stock goes when an other-use request is approved." },
      ],
    },
    {
      id: "otherUse",
      title: "Other-use request status",
      group: "otherUse",
      entries: [
        { value: "PENDING", meaning: "Waiting for a decision. No stock has moved." },
        { value: "APPROVED", meaning: "Approved; the stock has left its locations." },
        { value: "REJECTED", meaning: "Turned down; nothing moved." },
      ],
    },
    {
      id: "itemType",
      title: "Item types",
      intro: "Just a label for sorting the catalogue — any item can be used in any recipe.",
      group: "itemType",
      entries: [
        { value: "PART", meaning: "Bought-in parts and raw materials." },
        { value: "SEMI_FINISHED", meaning: "Made here and used to make something else." },
        { value: "FINISHED", meaning: "A finished product." },
      ],
    },
    {
      id: "telegram",
      title: "Telegram link",
      intro: "Shown on People → Employees.",
      group: "telegram",
      entries: [
        { value: "LINKED", meaning: "This person can use the bot; the number is their Telegram id." },
        { value: "NOT_LINKED", meaning: "The bot will refuse them until they are linked." },
      ],
    },
  ],
  termsTitle: "Words used in this guide",
  terms: [
    { term: "Ledger", meaning: "The full list of stock movements. Nothing in it is ever changed or deleted." },
    { term: "Balance", meaning: "How much of an item is at a location, in a given quality — the sum of its movements." },
    { term: "Available", meaning: "Good stock in warehouses that no open work order has reserved." },
    { term: "Reservation", meaning: "Stock promised to a work order, so two jobs can't count on the same parts." },
    { term: "Reversal", meaning: "A new movement that undoes a wrong one by moving the stock back." },
    { term: "Facility float", meaning: "Cash in the till: funding received minus advances handed out." },
    { term: "Acting as", meaning: "The person the app records as doing things on this computer. Not a login." },
  ],
};
