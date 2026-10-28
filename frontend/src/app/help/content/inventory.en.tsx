import {
  AppstoreOutlined,
  EnvironmentOutlined,
  ExportOutlined,
  HistoryOutlined,
  InboxOutlined,
  RollbackOutlined,
  ToolOutlined,
} from "../guide/icons";

import {
  Callout,
  Example,
  ExampleTable,
  Fn,
  Formula,
  Guide,
  GuideHero,
  Op,
  Section,
  Step,
  Steps,
  Ui,
  V,
} from "../guide/Guide";

export default function InventoryEn() {
  return (
    <Guide>
      <GuideHero
        icon={<InboxOutlined />}
        eyebrow="User guide · Inventory"
        title="Stock: where everything is"
        lead="Every time something arrives, moves, is handed to a person or thrown away, you record a movement. Coperti adds them all up, so the Stock page always shows how much of each item is where — and how much is free to use."
      />

      <Section icon={<AppstoreOutlined />} title="How it fits together">
        <p>
          <strong>Locations</strong> are the places stock can be: warehouses, benches, quarantine — and each employee
          (stock handed to a person is “at” that person).
        </p>
        <p>
          <strong>Movements</strong> are the history: “12 screws went from W1 to Bob on Tuesday, done by Ann”. They are
          never edited or deleted.
        </p>
        <p>
          <strong>Stock</strong> (balances) is the sum of all movements, per item, location and quality.
        </p>
        <Formula name="Balance of an item at a location" note="Counted separately for each quality: Good, Defective, Quarantined.">
          <V result>Balance</V>
          <Op>=</Op>
          <Fn>Σ</Fn>
          <V>Quantity moved in</V>
          <Op>−</Op>
          <Fn>Σ</Fn>
          <V>Quantity moved out</V>
        </Formula>
        <Callout tone="warning" title="Stock can’t go below zero">
          A movement that would take more out of a real location than it holds is refused, with a message like{" "}
          <em>“Not enough Screw M4 (Good) at W1: 15 on hand, 100 requested”</em>.
        </Callout>
      </Section>

      <Section icon={<EnvironmentOutlined />} title="Set up your locations">
        <Steps>
          <Step title="Open Inventory → Locations and press “Add location”." />
          <Step title="Give it a short code (e.g. W1), a name, and a kind.">
            Use <strong>Warehouse</strong> for stores — only warehouses count towards stock that is free to use.
          </Step>
        </Steps>
        <p>
          You never create locations for people or the special “Supplier”, “Scrap”, “Consumed” and “Other use”
          locations: Coperti creates them by itself the first time they are needed. They are marked{" "}
          <Ui>automatic</Ui>. A location can be deleted only if nothing has ever moved through it.
        </p>
      </Section>

      <Section icon={<ExportOutlined />} title="Record a movement">
        <p>
          Press <Ui>Record movement</Ui> on the Stock or Movements page. First choose what happened — the form then
          asks only for what that kind of movement needs:
        </p>
        <Steps>
          <Step title="Receipt — a delivery arrived.">Choose the item, quantity and the location it went into.</Step>
          <Step title="Issue to employee — you gave stock to someone.">
            Choose where it came from and who received it. From now on it counts as being with that person.
          </Step>
          <Step title="Return from employee — someone brought stock back.">Choose who and where it went.</Step>
          <Step title="Transfer — moved between two of your locations." />
          <Step title="Quality change — found defective (or fine again).">
            The stock stays where it is; choose the quality before and after.
          </Step>
          <Step title="Return to supplier, Scrap — stock leaves for good." />
        </Steps>
        <p>
          <Ui>Performed by</Ui> is filled in from <Ui>Acting as</Ui>. Add a <Ui>Note</Ui> whenever the reason isn’t
          obvious.
        </p>
        <Callout tone="tip" title="Hand out stock from your phone">
          Linked employees can record deliveries and hand-outs with the Telegram bot — see the People guide.
        </Callout>
      </Section>

      <Section icon={<InboxOutlined />} title="Read the Stock page">
        <p>
          Filter by item, location or <Ui>Held by</Ui> (what a person has). Click an item name to see all its
          movements. Pick an item in the filter to see three figures:
        </p>
        <Formula name="Available — free to use" note="Reservations are made by work orders (production) so two jobs can’t count on the same stock.">
          <V result>Available</V>
          <Op>=</Op>
          <V>Good stock in warehouses</V>
          <Op>−</Op>
          <V>Reserved for open work orders</V>
        </Formula>
        <Formula name="Reserved">
          <V result>Reserved</V>
          <Op>=</Op>
          <V>In warehouses</V>
          <Op>−</Op>
          <V>Available</V>
        </Formula>
        <Example title="Worked example">
          <ExampleTable
            rows={[
              ["Received into W1", "30"],
              ["Given to Bob", "− 12"],
              ["Moved to the bench", "− 5"],
              ["Bob returned", "+ 2"],
              ["Good in warehouses → Available (nothing reserved)", "15"],
            ]}
          />
          <p style={{ marginTop: 8 }}>
            The 10 with Bob and the 5 on the bench are still in stock, but not “available”: they are already out of the
            store.
          </p>
        </Example>
      </Section>

      <Section icon={<RollbackOutlined />} title="Fix a mistake: reverse">
        <p>
          On <Ui>Movements</Ui>, press <Ui>Reverse</Ui> on the wrong row and say why. Coperti adds a new movement that
          moves the same quantity back; the original stays visible, marked <Ui>Reversed</Ui>, and the new one is marked{" "}
          <Ui>Reversal</Ui>.
        </p>
        <Callout tone="warning" title="When you can’t reverse">
          A movement can be reversed only once; a reversal itself can’t be reversed; and movements made by work orders
          or other-use requests are corrected there instead. Reversing is also refused if the stock has already moved
          on — e.g. you moved 5 to the bench but 2 were since scrapped, so only 3 can come back.
        </Callout>
      </Section>

      <Section icon={<ToolOutlined />} title="Other use: samples, maintenance, tools">
        <p>Stock taken for anything other than production goes through a request, so it is approved and counted.</p>
        <Steps>
          <Step title="Open Inventory → Other use and press “New request”.">
            Add a line for each item: where it is taken from, its quality and the quantity. Give a reason.
          </Step>
          <Step title="Someone approves or rejects it.">
            They must be chosen under <Ui>Acting as</Ui>. Approving moves the stock immediately; rejecting moves
            nothing.
          </Step>
        </Steps>
        <Callout tone="note" title="All or nothing">
          If even one line doesn’t have enough stock, approval is refused and nothing moves — the request stays
          pending until there is enough, or it is rejected.
        </Callout>
      </Section>

      <Section icon={<HistoryOutlined />} title="Find out what happened">
        <p>
          The Movements filters answer most questions. For example, <em>“what did Bob receive this week?”</em>: choose
          location <strong>Bob</strong>, movement <strong>Issue to employee</strong>, and this week’s dates.
        </p>
        <p>
          If balances ever look wrong, <Ui>⋮</Ui> → <Ui>Rebuild balances from the ledger</Ui> on the Stock page
          recalculates every balance from the full history.
        </p>
      </Section>
    </Guide>
  );
}
