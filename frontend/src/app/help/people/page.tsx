import PeopleEn from "../content/people.en";
import PeopleUk from "../content/people.uk";
import { Localized } from "../guide/localized";

export default function HelpPeoplePage() {
  return <Localized pages={{ en: PeopleEn, uk: PeopleUk }} />;
}
