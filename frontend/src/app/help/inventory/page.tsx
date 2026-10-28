import InventoryEn from "../content/inventory.en";
import InventoryUk from "../content/inventory.uk";
import { Localized } from "../guide/localized";

export default function HelpInventoryPage() {
  return <Localized pages={{ en: InventoryEn, uk: InventoryUk }} />;
}
