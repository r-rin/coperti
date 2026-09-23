"use client";

import { Modal as AntModal, type ModalProps } from "antd";

/**
 * antd's Modal without the zoom/fade transitions — it opens and closes instantly.
 * ConfigProvider has no global switch for this, so every modal goes through here.
 * An empty transition name is antd's own opt-out (see `getTransitionName`).
 */
export function Modal(props: ModalProps) {
  return <AntModal transitionName="" maskTransitionName="" {...props} />;
}
