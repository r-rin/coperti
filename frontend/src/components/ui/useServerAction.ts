"use client";

import { App } from "antd";
import { useRouter } from "next/navigation";
import { useTransition } from "react";

import { useI18n } from "@/i18n/I18nProvider";

interface Result<T> {
  ok: boolean;
  error?: string;
  data?: T;
}

/**
 * Runs a Server Action from a Client Component: shows the backend's message on failure, a toast on
 * success, and re-renders the page from the server so every figure is fresh. Returns whether it worked,
 * so callers can close a dialog or reset a form only on success.
 */
export function useServerAction() {
  const router = useRouter();
  const { message } = App.useApp();
  const { t } = useI18n();
  const [pending, startTransition] = useTransition();

  function run<T>(
    work: () => Promise<Result<T>>,
    success?: string,
    onSuccess?: (data: T | undefined) => void,
  ) {
    startTransition(async () => {
      const result = await work();
      if (result.ok) {
        if (success) message.success(success);
        onSuccess?.(result.data);
        router.refresh();
      } else {
        message.error(result.error ?? t.common.somethingWentWrong);
      }
    });
  }

  return { pending, run };
}
