package top.sanqii.finance.ui.screen

import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Loop
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sanqii.finance.R
import top.sanqii.finance.room.FinanceDatabase
import top.sanqii.finance.room.store.Record
import top.sanqii.finance.ui.components.CenterRow
import top.sanqii.finance.ui.components.FinanceBaseFAB
import java.time.LocalDate

private const val ANIMATE_DURATION_MILLIS = 400

private fun <T> tweenSpec() = tween<T>(ANIMATE_DURATION_MILLIS)
private fun <T> tweenDelaySpec() = tween<T>(ANIMATE_DURATION_MILLIS, ANIMATE_DURATION_MILLIS)

@Composable
fun NewRecordBody(navController: NavController) {
    var amountValue by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val database = FinanceDatabase.getInstance(LocalContext.current)
    fun onRecordsNewed() {
        navController.popBackStack()
    }

    AnimatedVisibility(
        visible = state,
        enter = expandIn(expandFrom = Alignment.Center),
        exit = shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        NewIncomeRecordBody(
            amountValue,
            { amountValue = it },
        ) { amount, date, type ->
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val income = Record(amount = amount, date = date, type = type, isIncome = true)
                    Log.d("NEW_INCOME", income.toString())
                    database.getRecordDao().insertRecords(income)
                }
                onRecordsNewed()
            }

        }
    }
    AnimatedVisibility(
        visible = !state,
        enter = expandIn(expandFrom = Alignment.Center),
        exit = shrinkOut(shrinkTowards = Alignment.Center)
    ) {
        NewBillRecordBody(
            amountValue,
            { amountValue = it },
        ) { amount, date, type ->
            coroutineScope.launch {
                withContext(Dispatchers.IO) {
                    val bill = Record(amount = amount, date = date, type = type, isIncome = false)
                    Log.d("NEW_BILL", bill.toString())
                    database.getRecordDao().insertRecords(bill)
                }
                onRecordsNewed()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(end = 32.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        FinanceBaseFAB(onClick = { state = !state }) {
            Icon(imageVector = Icons.Default.Loop, contentDescription = null)
            Text(
                text = if (state) "??????" else "??????",
                Modifier.padding(start = 8.dp, top = 2.dp)
            )
        }
    }

}

@Composable
private fun NewIncomeRecordBody(
    amountValue: String,
    onAmountValueChanged: (String) -> Unit,
    onSubmit: (Float, LocalDate, String) -> Unit
) {
    //TODO: ??????????????????????????????
    val title = "????????????"
    val typeMap = mapOf(
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????")
    )
    NewBaseRecordBody(
        title,
        typeMap,
        amountValue,
        onAmountValueChanged,
        onSubmit
    )
}

@Composable
private fun NewBillRecordBody(
    amountValue: String,
    onAmountValueChanged: (String) -> Unit,
    onSubmit: (Float, LocalDate, String) -> Unit
) {
    //TODO: ??????????????????????????????
    val title = "????????????"
    val typeMap = mapOf(
        "????????????" to listOf("????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????"),
        "????????????" to listOf("?????????", "?????????", "?????????", "?????????"),
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????"),
        "????????????" to listOf("?????????", "?????????", "?????????", "?????????"),
        "????????????" to listOf("????????????", "????????????", "????????????", "????????????", "????????????", "????????????"),
        "????????????" to listOf("????????????", "????????????", "????????????")
    )
    NewBaseRecordBody(
        title,
        typeMap,
        amountValue,
        onAmountValueChanged,
        onSubmit
    )
}

// ????????????,?????????????????????
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun NewBaseRecordBody(
    title: String,
    typeMap: Map<String, List<String>>,
    amountValue: String,
    onAmountValueChanged: (String) -> Unit,
    onSubmit: (Float, LocalDate, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxHeight()
            // ?????????clickable???????????????????????????????????????
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        focusManager.clearFocus()
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val (amountField, dateField, typeField) = remember { FocusRequester.createRefs() }
            val dateNow = LocalDate.now()
            var dateValue by remember { mutableStateOf(dateNow.toString()) }

            var chosenExpanded by remember { mutableStateOf(false) }
            var innerChosenExpanded by remember { mutableStateOf(false) }

            val submitExpanded = !(chosenExpanded || innerChosenExpanded)

            val picker = dateNow.run {
                // Log.d("INIT", "$year-$monthValue-$dayOfMonth")
                DatePickerDialog(
                    LocalContext.current,
                    //TODO: ?????????????????????????????????????????????????????????
                    { _, yearValue, monthValue, dayValue ->
                        val date = LocalDate.of(yearValue, monthValue + 1, dayValue)
                        // Log.d("PICK", date.toString())
                        dateValue = date.toString()
                    },
                    year,
                    monthValue - 1,
                    dayOfMonth
                )
            }

            CenterRow { Text(text = title, style = MaterialTheme.typography.caption) }

            // ???????????????
            var amountIsError by remember { mutableStateOf(false) }
            CenterRow {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(amountField)
                        .onFocusChanged {
                            if (it.isFocused) {
                                amountIsError = false
                            }
                        },
                    value = amountValue,
                    onValueChange = {
                        val pattern = Regex("(\\d*)|(\\d+\\.?\\d{0,2})")
                        if (pattern.matches(it)) onAmountValueChanged(it)
                    },
                    singleLine = true,
                    isError = amountIsError,
                    label = { Text("??????") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.baseline_currency_yen_24),
                            null
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            typeField.requestFocus()
                        }
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // ???????????????
            CenterRow {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(dateField)
                        .onFocusChanged {
                            if (it.isFocused) {
                                picker.show()
                            }
                        },
                    value = dateValue,
                    onValueChange = { dateValue = it },
                    readOnly = true,
                    singleLine = true,
                    label = { Text("??????") },
                    leadingIcon = { Icon(Icons.Filled.CalendarToday, null) },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            typeField.requestFocus()
                        }
                    )
                )
            }

            Spacer(Modifier.height(24.dp))

            // ???????????????
            var typeValue by remember { mutableStateOf("") }
            var typeIsError by remember { mutableStateOf(false) }
            val selectedDate = LocalDate.parse(dateValue)
            val submitInterceptor: () -> Unit = {
                when (amountValue.isEmpty()) {
                    true -> amountIsError = true
                    false -> when (typeValue.isEmpty()) {
                        true -> typeIsError = true
                        false -> {
                            onSubmit(
                                amountValue.toFloat(),
                                selectedDate,
                                typeValue
                            )
                        }
                    }
                }
            }
            Column {
                CenterRow {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(typeField)
                            .onFocusChanged {
                                if (it.isFocused) {
                                    typeIsError = false
                                    chosenExpanded = true
                                } else {
                                    chosenExpanded = false
                                    innerChosenExpanded = false
                                }
                            },
                        value = typeValue,
                        onValueChange = { typeValue = it },
                        isError = typeIsError,
                        readOnly = true,
                        singleLine = true,
                        label = { Text("??????") },
                        leadingIcon = { Icon(Icons.Filled.FormatListBulleted, null) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                submitInterceptor()
                            }
                        )
                    )
                }

                var innerChoice by remember { mutableStateOf(listOf<String>()) }
                AnimatedVisibility(
                    visible = chosenExpanded,
                    enter = slideInHorizontally(tweenSpec()) + fadeIn(tweenSpec()),
                    exit = slideOutHorizontally(tweenSpec()) + fadeOut(tweenSpec())
                ) {
                    Column(
                        Modifier
                            .padding(top = 8.dp)
                            .height(280.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        typeMap.forEach {
                            CenterRow {
                                Box(
                                    Modifier.clickable {
                                        chosenExpanded = false
                                        innerChosenExpanded = true
                                        innerChoice = it.value
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = it.key,
                                        onValueChange = {},
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
                AnimatedVisibility(
                    visible = innerChosenExpanded,
                    enter = slideInHorizontally(tweenDelaySpec()) + fadeIn(tweenDelaySpec()),
                    exit = slideOutHorizontally(tweenSpec()) + fadeOut(tweenSpec())
                ) {
                    Column(
                        Modifier
                            .padding(top = 8.dp)
                            .height(256.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        innerChoice.forEach {
                            CenterRow {
                                Box(
                                    Modifier.clickable {
                                        typeValue = it
                                        innerChosenExpanded = false
                                    }
                                ) {
                                    OutlinedTextField(
                                        value = it,
                                        onValueChange = {},
                                        enabled = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                submitExpanded,
                enter = fadeIn(tweenDelaySpec()) + expandVertically(tweenDelaySpec()),
                exit = fadeOut(tweenDelaySpec()) + shrinkVertically(tweenDelaySpec(), Alignment.Top)
            ) {
                CenterRow(Modifier.padding(vertical = 32.dp)) {
                    OutlinedButton(onClick = { submitInterceptor() }) {
                        Text("??????", style = MaterialTheme.typography.overline)
                    }
                }
            }

            /*
            DisposableEffect(Unit) {
                amountField.requestFocus()
                onDispose { }
            }
             */
        }
    }
}