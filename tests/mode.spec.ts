/**
 * tests for the mode command
 */
import { test, expect } from "@playwright/test";

test.beforeEach(async ({ page }) => {
  await page.goto("http://localhost:8000/");
  await page.getByRole("button", { name: "Show REPL" }).click();
});

/**
 * test for switching modes when it actually changes
 */
test("switching to verbose adds it to history and changes the history", async ({
  page,
}) => {
  // command1: load a file
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/stars/ten-star.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  const allListItems = page.locator(".history-element .text-box");
  const first = await allListItems.nth(0);
  await expect(first).toContainText(
    "Output: File data/stars/ten-star.csv loaded successfully"
  );

  // command2: view a file, which produces that file
  await page.getByLabel("Command Input Box to type in commands").fill("view");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  const tableBody = page.locator("div table tbody");
  await expect(page.getByRole("table")).toBeVisible;
  const tableRow1 = tableBody.locator("tr:nth-child(1)");
  await expect(tableRow1.locator("td:nth-child(1)")).toContainText("StarID");
  await expect(tableRow1.locator("td:nth-child(2)")).toContainText(
    "ProperName"
  );
  await expect(tableRow1.locator("td:nth-child(3)")).toContainText("X");
  await expect(tableRow1.locator("td:nth-child(4)")).toContainText("Y");
  await expect(tableRow1.locator("td:nth-child(5)")).toContainText("Z");

  const tableRow2 = tableBody.locator("tr:nth-child(2)");
  await expect(tableRow2.locator("td:nth-child(1)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(2)")).toContainText("Sol");
  await expect(tableRow2.locator("td:nth-child(3)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(4)")).toContainText("0");
  await expect(tableRow2.locator("td:nth-child(5)")).toContainText("0");

  // command3: change mode to verbose
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  await page.getByPlaceholder("Enter command here!").fill("mode verbose");
  await page.getByPlaceholder("Enter command here!").press("Enter");

  //////////////////// test that the history has changed to verbose

  // command 1
  const history = await page.locator("repl-history");
  expect(history).toBeVisible;
  await expect(page.getByRole("listitem")).toHaveCount(3);
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(0)
  ).toContainText("Command: load data/stars/ten-star.csv");
  await expect(
    page.getByRole("listitem").nth(0).getByRole("paragraph").nth(1)
  ).toContainText("Output: File data/stars/ten-star.csv loaded successfully");

  // command 2
  await expect(
    page.getByRole("listitem").nth(1).getByRole("paragraph").nth(0)
  ).toContainText("Command: view");
  await expect(
    page.getByRole("listitem").nth(1).getByRole("paragraph").nth(1)
  ).toContainText("Output:");
  await expect(
    page
      .getByRole("listitem")
      .nth(1)
      .getByRole("paragraph")
      .nth(1)
      .getByRole("table")
  ).toBeVisible;

  // command 3
  await expect(
    page.getByRole("listitem").nth(2).getByRole("paragraph").nth(0)
  ).toContainText("Command: mode verbose");
  await expect(
    page.getByRole("listitem").nth(2).getByRole("paragraph").nth(1)
  ).toContainText("Output: Mode changed to verbose");

  // test that new commands are verbose
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(
    page.getByRole("listitem").nth(3).getByRole("paragraph").nth(0)
  ).toContainText("Command: load data/census/dol_ri_earnings_disparity.csv");
  await expect(
    page.getByRole("listitem").nth(3).getByRole("paragraph").nth(1)
  ).toContainText(
    "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
  );

  // change back to brief
  await page.getByPlaceholder("Enter command here!").fill("mode brief");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(
    page.getByRole("listitem").nth(4).getByRole("paragraph")
  ).toContainText("Output: Mode changed to brief");
});

/**
 * test for switching to the same mode
 */
test("switching to the same mode as default adds it to history", async ({
  page,
}) => {
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  await page.getByPlaceholder("Enter command here!").fill("mode brief");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(page.locator(".repl-history ul")).toBeVisible();
  const ListItem = page.locator("li:nth-child(1)");
  await expect(ListItem.locator("div")).toContainText("Mode changed to brief");
});

/**
 * test for invalid mode command. This results in an alert, not adding to history
 */
test("submitting an invalid mode command adds it to the history", async ({
  page,
}) => {
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  await page.getByPlaceholder("Enter command here!").fill("mode jdjdjdjd");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(page.locator(".repl-history ul")).toBeVisible();
});

// /**
//  * test for invalid mode command doesn't increment the button
//  */
// test("submitting a mode (invalid/valid) command doesn't increment the button count", async ({
//   page,
// }) => {
//   await expect(
//     page.getByLabel("Command Input Box to type in commands")
//   ).toBeVisible();
//   await page
//     .getByLabel("Command Input Box to type in commands")
//     .fill("mode jdjdjdjd");
//   const button = page.getByRole("button");
//   const initialLabelText = await button.innerText();
//   const initialCommandCount = Number(initialLabelText);
//   await button.click();
//   await expect(page.locator(".repl-history ul")).toBeHidden();

//   const updatedLabelText = await button.innerText();
//   const updatedCommandCount = Number(updatedLabelText);
//   await expect(updatedCommandCount).toBe(initialCommandCount);

//   await page
//     .getByLabel("Command Input Box to type in commands")
//     .fill("mode verbose");
//   button.click();
//   await expect(page.locator(".repl-history ul")).toBeVisible();
//   const updatedLabelTextAfterValid = await button.innerText();
//   const updatedCommandCountAfterValid = Number(updatedLabelText);
//   await expect(updatedCommandCountAfterValid).toBe(initialCommandCount);
//   await expect(updatedLabelTextAfterValid).toBe(initialLabelText);
// });

/**
 * another test for changing mode updates the display
 */
test("changing the mode updates the display", async ({ page }) => {
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  const modeChangeCommand = "mode verbose";
  await page.getByPlaceholder("Enter command here!").fill(modeChangeCommand);
  await page.getByPlaceholder("Enter command here!").press("Enter");
  const parentElement = page.locator(".repl-history ul");
  const divCount = await parentElement.locator("div").count();
  expect(divCount).toBe(2);
});

/**
 * test for changing the mode to verbose and back to brief
 */
test("changing the mode to verbose and back to brief functions properly", async ({
  page,
}) => {
  await expect(page.getByPlaceholder("Enter command here!")).toBeVisible();
  const modeChangeCommand = "mode verbose";
  await page.getByPlaceholder("Enter command here!").fill(modeChangeCommand);
  await page.getByPlaceholder("Enter command here!").press("Enter");
  const parentElement = page.locator(".repl-history ul");
  const divCount = await parentElement.locator("div").count();
  expect(divCount).toBe(2);

  await page.getByPlaceholder("Enter command here!").fill("mode brief");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  const parentElement2 = page.locator(".repl-history ul");
  const divCount2 = await parentElement2.locator("div").count();
  expect(divCount2).toBe(4); // 4 divs total in all list elements
});
