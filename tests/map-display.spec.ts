import { test, expect } from "@playwright/test";

/**
testing basic functionality of the app (elements appear,
can click the submit button, can push commands, etc)
and testing invalid inputs
*/

// If you needed to do something before every test case...
test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
});

test("on page load, i see a REPL button", async ({ page }) => {
  await expect(page.getByRole("button", { name: "Show REPL" })).toBeVisible();
});

test("on page load, i see a Map button", async ({ page }) => {
  await expect(page.getByRole("button", { name: "Show REPL" })).toBeVisible();
  await page.getByRole("button", { name: "Show REPL" }).click();
  await expect(page.getByRole("button", { name: "Show REPL" })).toBeHidden();
  await expect(page.getByRole("button", { name: "Show Map" })).toBeVisible();
});

test("when broadband is called, I see popup with broadband info", async ({
  page,
}) => {
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("broadband California Ventura");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(
    page.getByText(
      "State: CaliforniaCounty: VenturaBroadband Access Percent: 91.7"
    )
  ).toBeVisible();
});
test("when broadband w/ invalid state or county is called, I see popup with error message", async ({
  page,
}) => {
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("broadband invalidState invalidCounty");
  await expect(
    page.once("dialog", (dialog) => {
      expect(dialog.message()).toContain(
        "Failed to retrieve broadband data: State invalidState is not a valid state name."
      );
    })
  ).toBeTruthy;
});
test("when invalid broadband w/ missing parameter is called, I see popup with error message", async ({
  page,
}) => {
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("broadband California");
  await expect(
    page.once("dialog", (dialog) => {
      expect(dialog.message()).toContain(
        "Invalid broadband retrieval command. Usage: broadband <state> <county>"
      );
    })
  ).toBeTruthy;
});
test("when i click on map, the name and county is displayed", async ({
  page,
}) => {
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("broadband Rhode_Island Providence");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(page.getByLabel("Map", { exact: true })).toBeVisible();
  // await page.getByLabel("Map", { exact: true }).click({
  //   position: {
  //     x: 586,
  //     y: 349,
  //   },
  // });
  await expect(page.getByLabel("Map", { exact: true })).toBeVisible();
  await expect(
    page.getByText(
      "State: Rhode IslandCounty: ProvidenceBroadband Access Percent: 85.4"
    )
  ).toBeVisible();
});

test("mocked search for a keyword functional on the frontend", async ({
  page,
}) => {
  await page.getByPlaceholder("Enter command here!").fill("mocksearch mock");
  await expect(
    page.once("dialog", (dialog) => {
      expect(dialog.message()).toContain("Mocked data overlayed successfully");
    })
  ).toBeTruthy;
});
