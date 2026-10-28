import ExpensesEn from "../content/expenses.en";
import ExpensesUk from "../content/expenses.uk";
import { Localized } from "../guide/localized";

export default function HelpExpensesPage() {
  return <Localized pages={{ en: ExpensesEn, uk: ExpensesUk }} />;
}
