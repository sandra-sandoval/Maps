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

/**
 * Don't worry about the "async" yet. We'll cover it in more detail
 * for the next sprint. For now, just think about "await" as something
 * you put before parts of your test that might take time to run,
 * like any interaction with the page.
 */
test("on page load, i see an input bar", async ({ page }) => {
  // Notice: http, not https! Our front-end is not set up for HTTPs.
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
});

test("after I type into the input box, its text changes", async ({ page }) => {
  // Step 1: Navigate to a URL
  // Step 2: Interact with the page
  // Locate the element you are looking for
  await page.getByLabel("Command Input Box to type in commands").click();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("Awesome command");

  // Step 3: Assert something about the page
  // Assertions are done by using the expect() function
  const mock_input = `Awesome command`;
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toHaveValue(mock_input);
});

test("input field for commands is functional", async ({ page }) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page
    .getByLabel("Command Input Box to type in commands")
    .fill("load_file data/dataset1.csv");
  const inputText = await page
    .getByLabel("Command Input Box to type in commands")
    .inputValue();
  await expect(inputText).toBe("load_file data/dataset1.csv");
});

test("input field for commands is functional before entering a command", async ({
  page,
}) => {
  await expect(
    page.getByLabel("Command Input Box to type in commands")
  ).toBeVisible();
  await page.getByLabel("Command Input Box to type in commands").fill("");
  const inputText = await page
    .getByLabel("Command Input Box to type in commands")
    .inputValue();
  await expect(inputText).toBe("");
});

test("on page load, i see a button", async ({ page }) => {
  await expect(page.getByRole("button").first()).toBeVisible();
});

/**
 * test for submitting an invalid command
 */
test("submitting an invalid command adds it to the history", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("this is a test");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      'Output: Command not found: this. Input "register <commandName> <function>" to re'
    )
  ).toBeVisible();
});

/**
 * test for submitting an empty command
 */
test("submitting an empty command doesn't add it to history", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").fill("");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await expect(page.locator(".repl-history ul")).toBeHidden();
  expect(
    page.once("dialog", (dialog) => {
      console.log(`Dialog message: ${dialog.message()}`);
      dialog.dismiss().catch(() => {});
    })
  );
});

/**
 * test for submitting various commands
 */
test("submitting various commands adds the appropriate ones to history", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
    )
  ).toBeVisible();
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("load hdjfdjffb");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: File not found")).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/dataset7.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: File not found").nth(1)).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("mode verbose");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Command: mode verbose")).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("mode fhjhfhfhfh");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText("Output: Invalid mode: fhjhfhfhfh. Use brief or verbose")
  ).toBeVisible();
});

test("registering a command works properly with load/view/search", async ({
  page,
}) => {
  await page;
  // await page.getByLabel("Command Input Box to type in commands").fill("load2 data/stars/stardata.csv");
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load2 data/stars/stardata.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      'Output: Command not found: load2. Input "register <commandName> <function>" to r'
    )
  ).toBeVisible();

  await page.getByPlaceholder("Enter command here!").fill("mode verbose");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: Mode changed to verbose")).toBeVisible();
});

//load tests---------------------------------------------------------------------------
test("invalid load with fileNotFound and invalid parameters ", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("load dghsjka");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: File not found")).toBeVisible();
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("load");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText("Output: Invalid usage of 'load' command. Usage: load <URL>")
  ).toBeVisible();
});

test("load, view , load , view ", async ({ page }) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/custom/dataset3.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText("Output: File data/custom/dataset3.csv loaded successfully")
  ).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("view");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      "Output: TypeColorBrandshirtblackP&BjeansblueH&MblazerblackH&M"
    )
  ).toBeVisible();
  expect(page.getByRole("table")).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/census/dol_ri_earnings_disparity.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      "Output: File data/census/dol_ri_earnings_disparity.csv loaded successfully"
    )
  ).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("view");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      "Output: StateData TypeAverage Weekly EarningsNumber of WorkersEarnings Disparity"
    )
  ).toBeVisible();
  expect(
    page.locator("table").filter({
      hasText:
        "StateData TypeAverage Weekly EarningsNumber of WorkersEarnings DisparityEmployed",
    })
  ).toBeVisible();
});

//---------------------------------------------------------------------------------------------------
//view tests-----------------------------------------------------------------------------------------
// test("loading and viewing empty csv should result in message ", async ({
//   page,
// }) => {
//   await page.getByRole("button", { name: "Show REPL" }).click();
//   await page.getByPlaceholder("Enter command here!").click();
//   await page
//     .getByPlaceholder("Enter command here!")
//     .fill("load data/custom/empty.csv");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(
//     page.getByText("Output: File data/custom/empty.csv loaded successfully")
//   ).toBeVisible();

//   await page.getByPlaceholder("Enter command here!").click();
//   await page.getByPlaceholder("Enter command here!").fill("view");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(page.getByText("Output: No data to display")).toBeVisible();
// });

// test("loading and viewing differently formatted csv's ", async ({ page }) => {
//   await page.getByRole("button", { name: "Show REPL" }).click();
//   await page.getByPlaceholder("Enter command here!").click();
//   //csv with quotes
//   await page
//     .getByPlaceholder("Enter command here!")
//     .fill("load data/custom/tall_phrase_data_with_quotes.csv");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(
//     page.getByText(
//       "Output: File data/custom/tall_phrase_data_with_quotes.csv loaded successfully"
//     )
//   ).toBeVisible();
//   await page.getByPlaceholder("Enter command here!").click();
//   await page.getByPlaceholder("Enter command here!").fill("view");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(page.getByRole("table")).toBeVisible();
//   expect(page.getByRole("cell", { name: '"hi, what\'s up?"' })).toBeVisible();
//   // expect(page.getByRole('cell', { name: '"how are you?"' })).toBeVisible();

//   await page.getByPlaceholder("Enter command here!").click();
//   //csv with singular row
//   await page
//     .getByPlaceholder("Enter command here!")
//     .fill("load data/custom/single_row.csv");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   await page.getByPlaceholder("Enter command here!").click();
//   await page.getByPlaceholder("Enter command here!").fill("view");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(page.getByText("Output: hudajuliapartners")).toBeVisible();
//   expect(page.getByRole("cell", { name: "huda" })).toBeVisible();
//   expect(page.getByRole("cell", { name: "julia" })).toBeVisible();
//   expect(page.getByRole("cell", { name: "partners" })).toBeVisible();

//   await page.getByPlaceholder("Enter command here!").click();
//   //singular col
//   await page
//     .getByPlaceholder("Enter command here!")
//     .fill("load data/custom/single_column.csv");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   await page.getByPlaceholder("Enter command here!").click();
//   await page.getByPlaceholder("Enter command here!").fill("view");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(page.locator("table").filter({ hasText: "thisisacsv" })).toBeVisible();
// });

// test("viewing without loading a file will result in error message", async ({
//   page,
// }) => {
//   await page.getByRole("button", { name: "Show REPL" }).click();
//   await page.getByPlaceholder("Enter command here!").click();
//   await page.getByPlaceholder("Enter command here!").fill("view");
//   await page.getByPlaceholder("Enter command here!").press("Enter");
//   expect(page.getByText("Output: CSV file not loaded")).toBeVisible();
// });

//search tests--------------------------------------------------------------------------------------------------
test(" invalid search request with wrong number of paramaters ", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/custom/dataset3.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await page.getByPlaceholder("Enter command here!").fill("search");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page.getByText(
      "Output: Invalid search command. Usage: search <hasHeaders> <value> <columnId>"
    )
  ).toBeVisible();

  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("search y black");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(
    page
      .getByText(
        "Output: Invalid search command. Usage: search <hasHeaders> <value> <columnId>"
      )
      .nth(1)
  ).toBeVisible();
});

test("search without loading file will result in an error message", async ({
  page,
}) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("search y black 1");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: CSV file not loaded")).toBeVisible();
});

test("search without headers", async ({ page }) => {
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/custom/single_column.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await page.getByPlaceholder("Enter command here!").fill("search n csv 0");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: csv")).toBeVisible();
});

test(" valid search request with headers ", async ({ page }) => {
  //valid results
  await page.getByRole("button", { name: "Show REPL" }).click();
  await page.getByPlaceholder("Enter command here!").click();
  await page
    .getByPlaceholder("Enter command here!")
    .fill("load data/custom/dataset3.csv");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  await page.getByPlaceholder("Enter command here!").fill("search y black 1");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: shirtblackP&BblazerblackH&M")).toBeVisible();

  //no results
  await page.getByPlaceholder("Enter command here!").click();
  await page.getByPlaceholder("Enter command here!").fill("search y hello 1");
  await page.getByPlaceholder("Enter command here!").press("Enter");
  expect(page.getByText("Output: No data to display")).toBeVisible();
});
