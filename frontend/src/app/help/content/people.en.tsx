import { KeyOutlined, MessageOutlined, NumberOutlined, TeamOutlined, UserAddOutlined } from "../guide/icons";

import { Callout, Guide, GuideHero, Section, Step, Steps, Ui } from "../guide/Guide";

export default function PeopleEn() {
  return (
    <Guide>
      <GuideHero
        icon={<TeamOutlined />}
        eyebrow="User guide · People"
        title="People and the Telegram bot"
        lead="Everyone who works here is listed on the People page. Linking a person’s Telegram account lets them record deliveries and hand out stock straight from their phone."
      />

      <Section icon={<UserAddOutlined />} title="Add, rename or remove a person">
        <p>
          Open <Ui>People</Ui> → <Ui>Employees</Ui>. Use <Ui>Add employee</Ui>, <Ui>Rename</Ui> and <Ui>Delete</Ui>{" "}
          on each row.
        </p>
        <Callout tone="note" title="Why a person sometimes can’t be deleted">
          Once someone has any history — receipts, advances, stock movements, tasks or a Telegram link — they can’t be
          deleted, because those records must keep saying who was involved.
        </Callout>
      </Section>

      <Section icon={<KeyOutlined />} title="Link Telegram with a code (easiest)">
        <Steps>
          <Step title="On the person’s row, open “Telegram” → “Issue link code”.">
            A window shows a command such as <Ui>/start 4CPBUFYM</Ui>, with a copy button.
          </Step>
          <Step title="Give it to the person.">
            They open the workshop’s bot in Telegram and send the command. If a <strong>t.me</strong> link is shown,
            they can simply tap it instead.
          </Step>
          <Step title="Done.">The row shows <Ui>Linked</Ui> with their Telegram number after a page refresh.</Step>
        </Steps>
        <Callout tone="warning" title="Codes are short-lived">
          A code works once, for 15 minutes. Issuing a new code cancels the previous one — so if it doesn’t work, just
          issue a fresh code.
        </Callout>
      </Section>

      <Section icon={<NumberOutlined />} title="Link Telegram by number">
        <p>
          When someone not yet linked writes to the bot, it replies with their Telegram number. Use{" "}
          <Ui>Telegram</Ui> → <Ui>Link by Telegram id</Ui> and type that number. Each Telegram account can belong to
          only one person.
        </p>
        <p>
          <Ui>Unlink Telegram</Ui> stops the person from using the bot immediately.
        </p>
      </Section>

      <Section icon={<MessageOutlined />} title="Using the bot (for the person on their phone)">
        <Steps>
          <Step title="/receive — a delivery arrived.">
            Type part of the item’s name, tap the item, tap the warehouse (picked automatically if there is only one),
            type the quantity and press <Ui>Confirm</Ui>.
          </Step>
          <Step title="/give — hand stock to a colleague.">
            Find the item; the bot offers only warehouses that have it, showing how much; pick the colleague, type the
            quantity, <Ui>Confirm</Ui>. It won’t let you give more than is there.
          </Step>
          <Step title="/cancel stops at any point; /help lists the commands." />
        </Steps>
        <Callout tone="tip" title="Everything the bot records shows up here">
          Bot movements appear on Inventory → Movements with the person as <Ui>Performed by</Ui> and the note “via
          Telegram”. They follow exactly the same rules as the web app.
        </Callout>
      </Section>
    </Guide>
  );
}
