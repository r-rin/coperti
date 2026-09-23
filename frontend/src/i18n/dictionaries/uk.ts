import type { Dictionary } from "./en";

const plural = new Intl.PluralRules("uk-UA");

/** Ukrainian has three count forms: 1 витрата, 2 витрати, 5 витрат. */
function count(n: number, one: string, few: string, many: string) {
  const form = plural.select(n);
  return `${n} ${form === "one" ? one : form === "few" ? few : many}`;
}

export const uk: Dictionary = {
  meta: {
    description: "Виробнича ERP — витрати, аванси та борги перед працівниками",
  },

  shell: {
    tagline: "Виробнича ERP",
    modules: "Модулі",
    language: "Мова",
  },

  nav: {
    expenses: "Витрати",
    overview: "Огляд",
    expenseList: "Витрати",
    advances: "Аванси",
    budgets: "Бюджети",
  },

  common: {
    date: "Дата",
    worker: "Працівник",
    amount: "Сума",
    description: "Опис",
    state: "Стан",
    from: "Від",
    edit: "Змінити",
    delete: "Видалити",
    cancel: "Скасувати",
    anyone: "Будь-хто",
    anyState: "Будь-який стан",
    filteredTotal: "Сума за фільтром",
    sumOfMatching: "Сума відповідних записів",
    fundedIn: "Надійшло",
    allBudgetRecords: "Усі бюджетні записи",
    facilityFloat: "Залишок у касі",
    remaining: "Залишок",
    fullyAccountedFor: "Повністю звітовано",
    nothingRecorded: "Поки нічого не записано.",
    somethingWentWrong: "Щось пішло не так",
    amountRequired: "Вкажіть суму",
    dateRequired: "Вкажіть дату",
  },

  status: {
    disbursement: { OPEN: "Відкритий", CLOSED: "Закритий", CANCELLED: "Скасований" },
  },

  error: {
    title: "Не вдалося завантажити сторінку",
    backendHintBefore: "Бекенд має працювати на порту 8080. Запустіть його командою",
    retry: "Спробувати ще раз",
  },

  overview: {
    title: "Огляд",
    subtitle:
      "Хто кому винен зараз: готівка в касі, скільки винні працівникам за їхні чеки та скільки готівки ще в них на руках.",
    tillNote: "Надходження мінус усі видані аванси",
    owedToWorkers: "Борг перед працівниками",
    owedNote: "Чеки, ще не покриті жодним авансом",
    unspentWithWorkers: "Невитрачене у працівників",
    unspentNote: "Залишок на відкритих авансах",
    afterPayingBack: "Залишиться після виплат",
    afterPayingBackNote: "Каса мінус борг перед працівниками",
    tillShort: "Каси не вистачає, щоб покрити борг",
    workersTitle: "Працівники",
    workersNote: "Спершу ті, кому винні найбільше. Кожна смуга ділить витрати працівника на покрите й ще не сплачене.",
    spent: "Витрачено",
    unspentColumn: "Невитрачений аванс",
    coverage: "Покриття",
    total: "Разом",
    noEmployees: "Працівників поки немає — додайте їх на сторінці «Витрати».",
    nothingSpent: "Витрат ще немає",
    showExpenses: (worker: string) => `Показати витрати: ${worker}`,
  },

  expenses: {
    title: "Витрати",
    subtitle:
      "Що працівники оплатили власним коштом. «Покрито» — те, що вже оплачено з авансів; «Борг» — те, що підприємство ще винне працівникові. Позначте чеки, щоб виплатити їх.",
    matching: "Знайдено витрат",
    matchingNote: "За поточними фільтрами",
    owedOnPage: "Борг на сторінці",
    owedOnPageNote: "Ще не покрито жодним авансом",
    filterTo: "До",
    containsPlaceholder: "Містить…",
    apply: "Застосувати",
    reset: "Скинути",
    tableTitle: "Витрати",
    addWorker: "Додати працівника",
    fullName: "Повне ім’я",
    workerAdded: "Працівника додано",
    record: "Записати витрату",
    editTitle: "Змінити витрату",
    covered: "Покрито",
    owed: "Борг",
    paySelected: (n: number) => `Виплатити вибране (${n})…`,
    clearSelection: "Скинути",
    oneWorkerHint: (worker: string) => `До цієї виплати можна додати лише чеки: ${worker}.`,
    payTitle: (worker: string) => `Виплата: ${worker}`,
    payOk: "Виплатити",
    receipts: (n: number) => count(n, "чек", "чеки", "чеків"),
    owedOnSelection: "Борг за вибраними",
    heldAdvance: "Невитрачений аванс на руках",
    heldAdvanceNote: "Використовується першим, до нової готівки",
    newCash: "Нова готівка до видачі",
    owedNow: "Борг зараз",
    owedAfter: "Борг після",
    fromAdvances: "З їхнього авансу",
    fromNewCash: "З нової готівки",
    stillOwed: "Лишиться винні",
    staysWithWorker: "Залишиться в них як аванс",
    nothingToPayWith: "Невитраченого авансу немає — вкажіть суму до видачі.",
    paidFull: "Виплачено — вибрані чеки повністю покрито",
    paidPartly: (amount: string) => `Виплачено — за вибраними ще винні ${amount}`,
    leftAsAdvance: (amount: string) => `${amount} залишається в працівника як відкритий аванс`,
    deleteConfirm: "Видалити цю витрату?",
    deleted: "Витрату видалено",
    updated: "Витрату оновлено",
    recorded: "Витрату записано",
    pickWorker: "Оберіть працівника, який платив",
    whoPaid: "Хто платив власним коштом?",
    whatBought: "Що було придбано?",
  },

  advances: {
    title: "Аванси",
    subtitle:
      "Готівка, видана працівникам. Аванс лишається відкритим, доки витрати не вичерпають його до нуля, — тоді він закривається автоматично.",
    unspent: "Невитрачене у працівників",
    unspentNote: "Залишок на всіх відкритих авансах",
    handOut: "Видати аванс",
    handedOut: "Аванс видано",
    tableTitle: "Аванси",
    allocate: "Розподілити",
    close: "Закрити",
    closeConfirm: "Закрити цей аванс?",
    closeConfirmNote: "Позначає його розрахованим. Так можна списати невеликий невитрачений залишок.",
    closed: "Аванс закрито",
    cancel: "Скасувати",
    cancelConfirm: "Скасувати цей аванс?",
    cancelConfirmNote:
      "Підтверджує, що гроші не виходили з каси. Неможливо, якщо з авансу вже щось оплачено.",
    cancelled: "Аванс скасовано",
    pickRecipient: "Оберіть, хто отримує готівку",
  },

  allocate: {
    back: "← Усі аванси",
    title: (worker: string) => `Аванс: ${worker}`,
    subtitle:
      "Позначте, які чеки цього працівника оплачено з цього авансу. Коли залишок стане нульовим, аванс закриється автоматично.",
    advance: "Аванс",
    drawnDown: "Використано",
    fundingRows: (n: number) => count(n, "запис оплати", "записи оплати", "записів оплати"),
    stillToAccount: "Ще треба прозвітувати",
    settlementState: "Стан розрахунку",
    cardTitle: "Розподіл на витрати",
    cardNote: "Показано лише витрати цього працівника, які ще не покрито повністю.",
    notAllocatable: (state: string) =>
      `Аванс має стан «${state}», тож з нього не можна нічого оплатити.`,
    allocatedClosed: "Розподілено — аванс повністю використано й закрито",
    allocatedRemaining: (amount: string) => `Розподілено. Залишок: ${amount}.`,
    nothingTitle: "Нічого розподіляти",
    nothingNote:
      "У цього працівника немає витрат з непокритою сумою. Спершу запишіть для нього витрату.",
    expense: "Витрата",
    stillNeeds: "Ще потрібно",
    cover: "Покрити з цього авансу",
    max: "Макс.",
    selected: "Вибрано",
    ofAvailable: (amount: string) => `з ${amount} доступних`,
    submit: (n: number) => (n ? `Розподілити на ${count(n, "витрату", "витрати", "витрат")}` : "Розподілити"),
    overTitle: (amount: string) => `Вибрана сума перевищує залишок на ${amount}`,
    overNote:
      "Вибір відхиляється повністю, а не частково, тож зменште суму перед розподілом.",
  },

  budgets: {
    title: "Бюджети",
    subtitle:
      "Кошти, що надійшли на підприємство, і скільки з них лишилося. Скасовані аванси не виходили з каси, тому не зменшують залишок.",
    floatNote: "Ще в касі",
    handedOut: "Видано",
    handedOutNote: "Відкриті й закриті аванси",
    whereMoneySits: "Де зараз гроші",
    whereMoneySitsNote:
      "Усе, що надійшло, або в касі, або ще в працівників, або вже витрачено на чеки (чи списано).",
    inTill: "У касі",
    withWorkers: "Невитрачене у працівників",
    usedUp: "Витрачено або списано",
    tableTitle: "Отримані кошти",
    record: "Записати надходження",
    recorded: "Надходження записано",
    whoProvided: "Хто надав кошти?",
  },
};
