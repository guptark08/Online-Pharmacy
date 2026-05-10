import { create } from "zustand";
import { persist } from "zustand/middleware";

export const useAuthStore = create(
  persist(
    (set) => ({
      token: "",
      user: null,
      setSession: (session) => set(session),
      logout: () => set({ token: "", user: null }),
    }),
    {
      name: "pharmacy-react-session",
      partialize: (state) => ({
        token: state.token,
        user: state.user,
      }),
    },
  ),
);
