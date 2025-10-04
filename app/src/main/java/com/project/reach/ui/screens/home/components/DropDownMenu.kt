package com.project.reach.ui.screens.home.components

// TODO handle mode selection
//@Composable
//fun DropDownMenu(viewModel: HomeScreenViewModel) {
//    var expanded by remember { mutableStateOf(false) }
//    var selectedOption by remember { mutableStateOf(ConnectionMode.WIFI) }
//    val connectionMode = ConnectionMode.entries
//    val trailingIconList = mapOf(
//        ConnectionMode.WIFI to Icons.Default.Wifi,
//        ConnectionMode.BLUETOOTH to Icons.Filled.Bluetooth,
//        ConnectionMode.WIFI_DIRECT to Icons.Filled.Wifi,
//    )
//    ExposedDropdownMenuBox(
//        expanded = expanded,
//        onExpandedChange = { expanded = !expanded }
//    ) {
//        Icon(
//            imageVector = trailingIconList[selectedOption]!!,
//            contentDescription = selectedOption.name,
//            modifier = Modifier
//                .menuAnchor()
//        )
//        ExposedDropdownMenu(
//            expanded = expanded,
//            modifier = Modifier.fillMaxWidth(0.3f),
//            onDismissRequest = { expanded = false }
//        ) {
//            connectionMode.forEach { option ->
//                DropdownMenuItem(
//                    text = { Text(option.name) },
//                    onClick = {
//                        viewModel.changeConnectionMode(option)
//                        selectedOption = option
//                        expanded = false
//                    }
//                )
//            }
//        }
//    }
//}