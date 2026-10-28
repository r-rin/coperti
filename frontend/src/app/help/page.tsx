import StartEn from "./content/start.en";
import StartUk from "./content/start.uk";
import { Localized } from "./guide/localized";

export default function HelpStartPage() {
  return <Localized pages={{ en: StartEn, uk: StartUk }} />;
}
