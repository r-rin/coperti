import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { AntdRegistry } from "@ant-design/nextjs-registry";
import { AppShell } from "@/components/shell/AppShell";
import { ThemeProvider } from "@/components/ThemeProvider";
import { I18nProvider } from "@/i18n/I18nProvider";
import { getI18n } from "@/i18n/server";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export async function generateMetadata(): Promise<Metadata> {
  const { t } = await getI18n();
  return { title: "Coperti", description: t.meta.description };
}

export default async function RootLayout({ children }: LayoutProps<"/">) {
  const { locale } = await getI18n();

  return (
    <html lang={locale} className={`${geistSans.variable} ${geistMono.variable}`}>
      <body>
        <AntdRegistry>
          <I18nProvider locale={locale}>
            <ThemeProvider>
              <AppShell>{children}</AppShell>
            </ThemeProvider>
          </I18nProvider>
        </AntdRegistry>
      </body>
    </html>
  );
}
