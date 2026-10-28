import {
  CompassOutlined,
  InboxOutlined,
  LinkOutlined,
  ReadOutlined,
  SafetyOutlined,
  TeamOutlined,
  UserSwitchOutlined,
  WalletOutlined,
} from "../guide/icons";

import { Callout, Cards, Guide, GuideCard, GuideHero, Section, Step, Steps, Ui } from "../guide/Guide";

export default function StartEn() {
  return (
    <Guide>
      <GuideHero
        icon={<CompassOutlined />}
        eyebrow="User guide"
        title="Getting started with Coperti"
        lead="Coperti keeps track of what your workshop spends, what it has in stock and who did what. This guide explains every screen in plain words — what to click, what happens, and how each number is worked out."
      />

      <Section icon={<ReadOutlined />} title="Guides">
        <Cards>
          <GuideCard href="/help/expenses" icon={<WalletOutlined />} title="Expenses">
            Receipts workers paid themselves, cash advances, paying people back and the money in the till.
          </GuideCard>
          <GuideCard href="/help/inventory" icon={<InboxOutlined />} title="Inventory">
            Where every item is, recording deliveries and hand-outs, fixing mistakes and taking stock for other use.
          </GuideCard>
          <GuideCard href="/help/people" icon={<TeamOutlined />} title="People & Telegram bot">
            Adding employees and letting them record supplies from their phone.
          </GuideCard>
          <GuideCard href="/help/glossary" icon={<ReadOutlined />} title="Glossary">
            What every status, type and coloured tag in the app means.
          </GuideCard>
        </Cards>
      </Section>

      <Section icon={<UserSwitchOutlined />} title="First: tell the app who you are">
        <p>
          Many actions record <strong>who</strong> did them — who received a delivery, who approved a request. At the
          bottom of the sidebar, pick your name under <Ui>Acting as</Ui>. The app remembers it on this computer, and
          forms fill in your name for you.
        </p>
        <Steps>
          <Step title="Open the “Acting as” box at the bottom left.">Start typing your name to find it quickly.</Step>
          <Step title="Pick your name.">The page refreshes; forms now show you as the person doing things.</Step>
          <Step title="Change it whenever someone else uses this computer.">
            You can still pick a different person inside any single form.
          </Step>
        </Steps>
        <Callout tone="warning" title="This is not a login">
          Anyone at this computer can choose any name. It exists so records say who did what; it does not protect
          anything. Proper sign-in will replace it later.
        </Callout>
        <p>
          The <Ui>Language</Ui> box below it switches the whole app between English and Ukrainian.
        </p>
      </Section>

      <Section icon={<SafetyOutlined />} title="Things that work the same everywhere">
        <p>
          <strong>Filters live in the address bar.</strong> When you filter a list, the address changes. Copy it to
          share exactly that view with a colleague, or bookmark it.
        </p>
        <p>
          <strong>Green and red messages.</strong> After every action a small message appears at the top: green when it
          worked, red when it was refused — with the reason in plain words, for example{" "}
          <em>“Not enough Screw M4 at W1: 15 on hand, 100 requested”</em>. Nothing is changed when it is red.
        </p>
        <p>
          <strong>History is never rewritten.</strong> Stock movements and money records are kept forever. A mistake is
          fixed by adding a correcting record, so you can always see what happened and when.
        </p>
        <p>
          <strong>Dangerous actions ask first.</strong> Deleting, cancelling and approving show a confirmation, so a
          stray click does nothing.
        </p>
        <Callout tone="tip" title="Can’t find something?">
          Each list has a counter next to its title, e.g. <Ui>Ledger (8)</Ui>. If it shows fewer rows than you expect,
          a filter is probably still on — press <Ui>Reset</Ui>.
        </Callout>
      </Section>

      <Section icon={<LinkOutlined />} title="When a page shows “Could not load this page”">
        <p>
          The app could not reach its server. Ask whoever runs Coperti to start it, then press <Ui>Try again</Ui>. Your
          data is safe — nothing is lost when this happens.
        </p>
      </Section>
    </Guide>
  );
}
