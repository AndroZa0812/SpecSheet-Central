import eslintPluginSvelte from "eslint-plugin-svelte";
import svelteParser from "svelte-eslint-parser";
import eslintPluginTs from "@typescript-eslint/eslint-plugin";
import eslintParserTs from "@typescript-eslint/parser";
import eslintConfigPrettier from "eslint-config-prettier";

/** @type {import('eslint').Linter.FlatConfig[]} */
export default [
  {
    ignores: ["node_modules", "dist", "build", ".svelte-kit", ".vercel"],
  },
  {
    files: ["**/*.svelte", "**/*.svelte.ts", "**/*.svelte.js"],
    languageOptions: {
      parser: svelteParser,
      parserOptions: {
        parser: eslintParserTs,
      },
    },
    plugins: {
      svelte: eslintPluginSvelte,
    },
    rules: {
      ...eslintPluginSvelte.configs.recommended.rules,
      "svelte/no-at-html-tags": "warn",
    },
  },
  {
    files: ["**/*.{js,ts,jsx,tsx}"],
    languageOptions: {
      parser: eslintParserTs,
      parserOptions: {
        ecmaVersion: "latest",
        sourceType: "module",
      },
    },
    plugins: {
      "@typescript-eslint": eslintPluginTs,
    },
    rules: {
      "@typescript-eslint/no-unused-vars": [
        "warn",
        { argsIgnorePattern: "^_" },
      ],
    },
  },
  eslintConfigPrettier,
];
