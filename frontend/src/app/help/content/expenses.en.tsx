import {
  BankOutlined,
  CalculatorOutlined,
  CreditCardOutlined,
  DollarOutlined,
  FilePdfOutlined,
  FileTextOutlined,
  SwapOutlined,
  WalletOutlined,
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

export default function ExpensesEn() {
  return (
    <Guide>
      <GuideHero
        icon={<WalletOutlined />}
        eyebrow="User guide · Expenses"
        title="Expenses, advances and the till"
        lead="Workers often pay for small things themselves, or get cash in advance to buy what the job needs. This module keeps both straight: what each receipt cost, what has already been paid for, and what the workshop still owes each person."
      />

      <Section icon={<FileTextOutlined />} title="Three things to know">
        <p>
          <strong>An expense</strong> is a receipt: a worker spent their own money on something for work.
        </p>
        <p>
          <strong>An advance</strong> is cash handed to a worker before they spend it. Receipts can then be “paid out
          of” the advance instead of out of the worker’s pocket.
        </p>
        <p>
          <strong>A budget record</strong> is money coming into the till — from the owner, a client payment, and so on.
          Advances are paid out of the till.
        </p>
      </Section>

      <Section icon={<CreditCardOutlined />} title="Record a receipt">
        <Steps>
          <Step title="Open Expenses → Expenses and press “Record expense”." />
          <Step title="Choose who paid, the amount, the date and what was bought.">
            The worker must exist first — use <Ui>Workers</Ui> to add them on the People page.
          </Step>
          <Step title="Press OK.">
            The receipt appears in the list with <Ui>Owed</Ui> equal to its full amount, until something covers it.
          </Step>
        </Steps>
        <p>
          Use <Ui>Edit</Ui> to correct a receipt. Tick <Ui>Only uncovered</Ui> and press <Ui>Apply</Ui> to see only
          receipts that still need paying.
        </p>
        <Formula name="Owed on one receipt" note="Covered is everything already paid towards this receipt, from any advances.">
          <V result>Owed</V>
          <Op>=</Op>
          <Fn>max</Fn>
          <Op>(</Op>
          <V>Amount</V>
          <Op>−</Op>
          <V>Covered</V>
          <Op>,</Op>
          <V>0</V>
          <Op>)</Op>
        </Formula>
      </Section>

      <Section icon={<DollarOutlined />} title="Hand out an advance">
        <Steps>
          <Step title="Open Expenses → Advances and press “Hand out advance”." />
          <Step title="Choose the worker, the amount and the date, then press OK.">
            The advance starts as <strong>Open</strong>: the worker holds that cash.
          </Step>
        </Steps>
        <Formula name="Left on an advance">
          <V result>Remaining</V>
          <Op>=</Op>
          <V>Advance amount</V>
          <Op>−</Op>
          <V>Already used for receipts</V>
        </Formula>
        <Callout tone="note" title="Advances close by themselves">
          When an advance’s remaining amount reaches zero it becomes <strong>Closed</strong> automatically — it has been
          fully accounted for.
        </Callout>
        <p>
          The <Ui>⋮</Ui> menu on each advance also lets you <Ui>Close</Ui> it by hand (the worker returned or settled
          the rest; whatever was left no longer counts as cash they hold) or <Ui>Cancel</Ui> it (the cash never
          actually left — only possible while nothing has been paid from it).
        </p>
      </Section>

      <Section icon={<SwapOutlined />} title="Cover receipts from an advance">
        <p>Use this when a worker brings back receipts for things they bought with advance money.</p>
        <Steps>
          <Step title="On the Advances page, press “Allocate” on the worker’s advance.">
            You see the advance, how much is left, and that worker’s receipts that still need money.
          </Step>
          <Step title="Type how much of each receipt this advance pays, or press “Max”.">
            “Max” fills in as much as possible without going over the advance or the receipt.
          </Step>
          <Step title="Press the Allocate button at the bottom.">The running total turns red if you ask for more than is left.</Step>
        </Steps>
        <Callout tone="warning" title="The rules">
          An advance can only pay for receipts of the same worker, only while it is Open, never more than it has left,
          and never more than a receipt still owes.
        </Callout>
      </Section>

      <Section icon={<CalculatorOutlined />} title="Pay a worker back">
        <p>Use this to settle a worker’s receipts in one go — using their unspent advance first, then fresh cash.</p>
        <Steps>
          <Step title="On Expenses → Expenses, tick the receipts to pay.">
            All ticked receipts must belong to one person (the payment goes to one person). Receipts with nothing owed
            can’t be ticked.
          </Step>
          <Step title="Press “Pay selected (…)”.">
            The dialog shows what is owed, the unspent advance the worker holds, and suggests how much new cash to
            hand over.
          </Step>
          <Step title="Adjust the new cash if needed and press “Pay”.">
            The table shows, receipt by receipt, what will still be owed afterwards — before anything is saved.
          </Step>
        </Steps>
        <Formula name="Suggested new cash">
          <V result>New cash</V>
          <Op>=</Op>
          <Fn>max</Fn>
          <Op>(</Op>
          <V>Owed on selection</V>
          <Op>−</Op>
          <V>Unspent advance they hold</V>
          <Op>,</Op>
          <V>0</V>
          <Op>)</Op>
        </Formula>
        <p>
          The money is applied in a fixed order: first the worker’s <strong>open advances, oldest first</strong>, then
          the <strong>new cash</strong>; each covers the <strong>oldest receipt first</strong>. If you hand over more
          new cash than is owed, the rest stays with the worker as a new open advance.
        </p>
        <Example title="Worked example">
          <p>Bob has receipts of 30 (Oct 1) and 50 (Oct 3), and 20 left on an open advance. You hand over 70.</p>
          <ExampleTable
            rows={[
              ["Owed on selection", "80"],
              ["From his advance (covers the Oct 1 receipt first)", "20"],
              ["From new cash: rest of Oct 1 (10) + Oct 3 (50)", "60"],
              ["Still owed", "0"],
              ["New cash left with Bob as a new advance", "10"],
            ]}
          />
        </Example>
      </Section>

      <Section icon={<BankOutlined />} title="The till and the overview">
        <p>
          Record money coming in on <Ui>Budgets</Ui> → <Ui>Record funding</Ui>. The <Ui>Overview</Ui> page then shows
          where the workshop stands:
        </p>
        <Formula
          name="Facility float — cash in the till"
          note="Cancelled advances are not counted: that cash never left. The float never shows below zero."
        >
          <V result>Facility float</V>
          <Op>=</Op>
          <Fn>max</Fn>
          <Op>(</Op>
          <V>All funding received</V>
          <Op>−</Op>
          <V>All open and closed advances</V>
          <Op>,</Op>
          <V>0</V>
          <Op>)</Op>
        </Formula>
        <Formula name="Owed to workers">
          <V result>Owed to workers</V>
          <Op>=</Op>
          <Fn>Σ</Fn>
          <V>Owed on every receipt</V>
        </Formula>
        <Formula name="Unspent with workers" note="Only open advances count; a closed advance’s remainder was written off.">
          <V result>Unspent with workers</V>
          <Op>=</Op>
          <Fn>Σ</Fn>
          <V>Remaining on every open advance</V>
        </Formula>
        <Formula name="Left after paying back" note="Shown in red when negative: the till cannot cover what is owed.">
          <V result>Left after paying back</V>
          <Op>=</Op>
          <V>Facility float</V>
          <Op>−</Op>
          <V>Owed to workers</V>
        </Formula>
        <p>
          On the Budgets page, <strong>“Where the money sits”</strong> splits everything handed out into three parts:{" "}
          <em>in the till</em> (the float), <em>with workers</em> (unspent on open advances) and <em>used up</em>{" "}
          (advances handed out minus what workers still hold).
        </p>
      </Section>

      <Section icon={<FilePdfOutlined />} title="Reports">
        <p>
          On the Advances page, tick one or more advances and press <Ui>Report (…)</Ui>, or use <Ui>⋮</Ui> →{" "}
          <Ui>PDF report</Ui> on a single advance. The PDF lists each advance with the receipts it paid for and what is
          still owed — ready to print or send.
        </p>
      </Section>
    </Guide>
  );
}
