@file:OptIn(ExperimentalFoundationApi::class)

package com.example.mobile.presentation.ui

import android.content.ClipData
import android.content.Context
import android.view.DragEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.mobile.data.model.ParticipantApiModel
import com.example.mobile.data.model.Task
import com.example.mobile.data.model.Zone
import com.example.mobile.presentation.TASK_PRIORITY_OPTIONS
import com.example.mobile.presentation.TASK_STATUS_OPTIONS
import com.example.mobile.presentation.eventStatusRu
import com.example.mobile.presentation.participationRoleRu
import com.example.mobile.presentation.taskPriorityRu
import com.example.mobile.presentation.taskStatusRu
import com.example.mobile.presentation.viewmodel.EventDetailViewModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

private const val ROLE_PERFORMER = "PERFORMER"

private const val KANBAN_ROW_HEIGHT_DP = 392

private val KANBAN_COLUMN_WIDTH = 176.dp

private const val KANBAN_TASK_CLIP_LABEL = "task_manager_kanban_task_id"

/** Id задачи из сессии перетаскивания: сначала [DragEvent.getLocalState], затем элементы [ClipData]. */
private fun taskIdFromKanbanDropEvent(ev: DragEvent, context: Context): String {
    when (val ls = ev.localState) {
        is String -> if (ls.isNotBlank()) return ls.trim()
        is CharSequence -> if (ls.isNotBlank()) return ls.toString().trim()
    }
    val clip = ev.clipData ?: return ""
    for (i in 0 until clip.itemCount) {
        val item = clip.getItemAt(i) ?: continue
        item.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { return it }
        val coerced = item.coerceToText(context)
        if (coerced.isNotEmpty()) return coerced.toString().trim()
    }
    return ""
}

/** Полоса у левого/правого края видимой дорожки канбана (в координатах окна). */
private val KANBAN_EDGE_SCROLL_MARGIN_DP = 24.dp

private val KANBAN_EDGE_SCROLL_STEP_DP = 20.dp

private const val KANBAN_EDGE_SCROLL_MIN_INTERVAL_MS = 72L

private val taskDeadlineDisplayFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm", Locale("ru", "RU"))

/** Строка API (ISO) или instant → подпись для карточки; при ошибке разбора возвращается исходная строка. */
private fun formatTaskDeadlineForCard(deadline: String?): String? {
    val raw = deadline?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val ldt = runCatching { LocalDateTime.parse(raw) }.getOrNull()
        ?: runCatching { Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalDateTime() }.getOrNull()
    return ldt?.format(taskDeadlineDisplayFormatter) ?: raw
}

private data class ZoneBoardDescriptor(
    val zoneId: String?,
    val title: String,
    val subtitle: String?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    viewModel: EventDetailViewModel,
    onBack: () -> Unit,
    onOpenInviteParticipants: () -> Unit = {},
    onOpenParticipants: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val boardDescriptors = remember(state.zones, state.tasks) {
        if (state.tasks.isEmpty()) emptyList()
        else buildList {
            val knownZoneIds = state.zones.map { it.id }.toSet()
            val zoneIdsFromTasks = state.tasks.mapNotNull { it.zone?.id }.toSet()
            if (state.zones.isNotEmpty()) {
                state.zones.forEach { z ->
                    add(
                        ZoneBoardDescriptor(
                            zoneId = z.id,
                            title = z.name,
                            subtitle = z.description?.takeIf { it.isNotBlank() }
                        )
                    )
                }
            }
            zoneIdsFromTasks.subtract(knownZoneIds).forEach { orphanId ->
                val sample = state.tasks.firstOrNull { it.zone?.id == orphanId }?.zone
                add(
                    ZoneBoardDescriptor(
                        zoneId = orphanId,
                        title = sample?.name ?: "Зона",
                        subtitle = sample?.description?.takeIf { it.isNotBlank() }
                    )
                )
            }
            if (state.tasks.any { it.zone == null }) {
                add(
                    ZoneBoardDescriptor(
                        zoneId = null,
                        title = if (state.zones.isEmpty() && zoneIdsFromTasks.isEmpty()) {
                            "Все задачи"
                        } else {
                            "Вне зон"
                        },
                        subtitle = if (state.zones.isEmpty() && zoneIdsFromTasks.isEmpty()) {
                            null
                        } else {
                            "Без привязки к зоне"
                        }
                    )
                )
            }
        }
    }
    var showZoneDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    var taskToDelete by remember { mutableStateOf<String?>(null) }

    if (showZoneDialog) {
        CreateZoneDialog(
            onDismiss = {
                showZoneDialog = false
                viewModel.clearError()
            },
            onConfirm = { name, desc, count ->
                viewModel.createZone(name, desc, count) { ok ->
                    if (ok) showZoneDialog = false
                }
            },
            errorText = state.formError
        )
    }

    if (showTaskDialog) {
        CreateTaskDialog(
            zones = state.zones,
            participants = state.participants,
            onDismiss = {
                showTaskDialog = false
                viewModel.clearError()
            },
            onConfirm = { title, desc, zoneId, priority, deadline, performerIds ->
                viewModel.createTask(title, desc, zoneId, priority, deadline, performerIds) { ok ->
                    if (ok) showTaskDialog = false
                }
            },
            errorText = state.formError
        )
    }

    taskToDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Удалить задачу?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTask(id)
                        taskToDelete = null
                    }
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Отмена") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.event?.name ?: "Мероприятие") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    TextButton(onClick = onOpenParticipants) {
                        Text("Участники")
                    }
                    if (state.canInviteParticipants) {
                        TextButton(onClick = onOpenInviteParticipants) {
                            Text("Пригласить")
                        }
                    }
                    TextButton(onClick = { viewModel.refresh() }) {
                        Text("Обновить")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (state.isLoading && state.event == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                state.event?.let { ev ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(ev.address, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Статус: ${eventStatusRu(ev.status)}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                state.cacheHint?.let { hint ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.dismissCacheHint() },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            item {
                if (state.errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearError() }
                    ) {
                        Text(
                            text = state.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showZoneDialog = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("Новая зона") }
                    Button(
                        onClick = { showTaskDialog = true },
                        modifier = Modifier.weight(1f)
                    ) { Text("Новая задача") }
                }
            }

            item {
                Text("Зоны", style = MaterialTheme.typography.titleMedium)
            }
            if (state.zones.isEmpty()) {
                item {
                    Text("Зон пока нет", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(state.zones, key = { it.id }) { zone ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(zone.name, style = MaterialTheme.typography.titleSmall)
                            zone.description?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Доска задач",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    "Колонки — статусы, строки — зоны. Удерживайте карточку и перетащите в другой статус; у левого и правого края видимой полосы дорожка прокручивается.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (state.tasks.isEmpty()) {
                item {
                    Text(
                        "Задач по этому мероприятию пока нет",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(boardDescriptors, key = { "${it.zoneId ?: "none"}-${it.title}" }) { lane ->
                    ZoneKanbanSwimlane(
                        descriptor = lane,
                        allTasks = state.tasks,
                        onStatusChange = { taskId, newStatus ->
                            viewModel.updateTaskStatus(taskId, newStatus)
                        },
                        onDelete = { id -> taskToDelete = id }
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoneKanbanSwimlane(
    descriptor: ZoneBoardDescriptor,
    allTasks: List<Task>,
    onStatusChange: (taskId: String, newStatus: String) -> Unit,
    onDelete: (String) -> Unit
) {
    val tasksInLane = remember(descriptor.zoneId, allTasks) {
        allTasks.filter { task ->
            when (descriptor.zoneId) {
                null -> task.zone == null
                else -> task.zone?.id == descriptor.zoneId
            }
        }
    }
    val density = LocalDensity.current
    val edgePx = remember(density) { with(density) { KANBAN_EDGE_SCROLL_MARGIN_DP.toPx() } }
    val stepPx = remember(density) { with(density) { KANBAN_EDGE_SCROLL_STEP_DP.toPx() } }
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val laneBoundsInWindow = remember { mutableStateOf<Rect?>(null) }
    val lastEdgeScrollAt = remember { mutableLongStateOf(0L) }

    /** [dragXInWindow] — X в координатах окна (левая граница колонки + локальный x из DragEvent). */
    val onLaneDragMoved = remember(scrollState, scope, edgePx, stepPx) {
        scrollHandler@{ dragXInWindow: Float ->
            val lane = laneBoundsInWindow.value ?: return@scrollHandler
            val inLeftEdge = dragXInWindow < lane.left + edgePx
            val inRightEdge = dragXInWindow > lane.right - edgePx
            if (inLeftEdge || inRightEdge) {
                val now = System.currentTimeMillis()
                if (now - lastEdgeScrollAt.longValue >= KANBAN_EDGE_SCROLL_MIN_INTERVAL_MS) {
                    lastEdgeScrollAt.longValue = now
                    when {
                        inLeftEdge -> scope.launch { scrollState.scroll { scrollBy(-stepPx) } }
                        inRightEdge -> scope.launch { scrollState.scroll { scrollBy(stepPx) } }
                    }
                }
            }
        }
    }

    Column(Modifier.fillMaxWidth()) {
        Text(descriptor.title, style = MaterialTheme.typography.titleSmall)
        descriptor.subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(KANBAN_ROW_HEIGHT_DP.dp)
                .onGloballyPositioned { coords ->
                    laneBoundsInWindow.value = coords.boundsInWindow()
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TASK_STATUS_OPTIONS.forEach { status ->
                    KanbanStatusColumn(
                        status = status,
                        columnTasks = tasksInLane.filter { it.status == status },
                        laneTasks = tasksInLane,
                        modifier = Modifier
                            .width(KANBAN_COLUMN_WIDTH)
                            .fillMaxHeight(),
                        onStatusChange = onStatusChange,
                        onDelete = onDelete,
                        onLaneDragMoved = onLaneDragMoved
                    )
                }
            }
        }
    }
}

@Composable
private fun KanbanStatusColumn(
    status: String,
    columnTasks: List<Task>,
    /** Все задачи дорожки (зона): нужны, чтобы принять drop из другой колонки — там задача ещё со старым статусом. */
    laneTasks: List<Task>,
    modifier: Modifier = Modifier,
    onStatusChange: (taskId: String, newStatus: String) -> Unit,
    onDelete: (String) -> Unit,
    /** X палец/курсора в координатах окна (для симметричного автоскролла по краям видимой дорожки). */
    onLaneDragMoved: (dragXInWindow: Float) -> Unit
) {
    val dropContext = rememberUpdatedState(LocalContext.current)
    val currentLaneTasks by rememberUpdatedState(laneTasks)
    val currentStatus by rememberUpdatedState(status)
    val currentOnStatus by rememberUpdatedState(onStatusChange)
    val dragMoved by rememberUpdatedState(onLaneDragMoved)
    val columnBoundsInWindow = remember { mutableStateOf<Rect?>(null) }
    val acceptAnyDragSession = remember {
        { _: DragAndDropEvent -> true }
    }
    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onMoved(event: DragAndDropEvent) {
                val col = columnBoundsInWindow.value ?: return
                val ev = try {
                    event.toAndroidDragEvent()
                } catch (_: Throwable) {
                    return
                }
                dragMoved(col.left + ev.x)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                val androidEv = try {
                    event.toAndroidDragEvent()
                } catch (_: Throwable) {
                    return false
                }
                val taskId = taskIdFromKanbanDropEvent(androidEv, dropContext.value)
                if (taskId.isEmpty()) return false
                val task = currentLaneTasks.find { it.id == taskId } ?: return false
                if (task.status == currentStatus) return true
                currentOnStatus(taskId, currentStatus)
                return true
            }
        }
    }

    Column(modifier) {
        Text(
            "${taskStatusRu(status)} · ${columnTasks.size}",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        // Цель drop на Box вокруг Surface: иначе verticalScroll внутри Surface перехватывает сессию и drop в чужую колонку не срабатывает.
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    columnBoundsInWindow.value = coords.boundsInWindow()
                }
                .dragAndDropTarget(
                    shouldStartDragAndDrop = acceptAnyDragSession,
                    target = dropTarget
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (columnTasks.isEmpty()) {
                        Text(
                            "Пусто",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .fillMaxWidth()
                        )
                    } else {
                        columnTasks.forEach { task ->
                            TaskKanbanCard(
                                task = task,
                                onStatusChange = { newStatus ->
                                    if (newStatus != task.status) onStatusChange(task.id, newStatus)
                                },
                                onDelete = { onDelete(task.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskKanbanCard(
    task: Task,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    var statusMenuOpen by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .dragAndDropSource { _ ->
                DragAndDropTransferData(
                    clipData = ClipData.newPlainText(KANBAN_TASK_CLIP_LABEL, task.id),
                    localState = task.id,
                    flags = 0
                )
            },
        colors = taskPriorityCardColors(task.priority),
        border = BorderStroke(
            width = 1.dp,
            color = taskPriorityAccentColor(task.priority).copy(alpha = 0.55f)
        )
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Удалить задачу",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            task.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }
            formatTaskDeadlineForCard(task.deadline)?.let { deadlineLine ->
                Text(
                    text = "До $deadlineLine",
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                taskPriorityRu(task.priority),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = taskPriorityAccentColor(task.priority),
                modifier = Modifier.padding(top = 4.dp)
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = taskStatusRu(task.status),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { statusMenuOpen = true }
                        .padding(vertical = 6.dp)
                )
                DropdownMenu(
                    expanded = statusMenuOpen,
                    onDismissRequest = { statusMenuOpen = false }
                ) {
                    TASK_STATUS_OPTIONS.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(taskStatusRu(s)) },
                            onClick = {
                                statusMenuOpen = false
                                onStatusChange(s)
                            }
                        )
                    }
                }
            }
            if (task.performers.isNotEmpty()) {
                Text(
                    text = task.performers.joinToString { it.email },
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun taskPriorityCardColors(priority: String): CardColors {
    val scheme = MaterialTheme.colorScheme
    return when (priority.uppercase()) {
        "HIGH" -> CardDefaults.cardColors(
            containerColor = scheme.errorContainer,
            contentColor = scheme.onErrorContainer
        )
        "MEDIUM" -> CardDefaults.cardColors(
            containerColor = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer
        )
        "LOW" -> CardDefaults.cardColors(
            containerColor = scheme.secondaryContainer,
            contentColor = scheme.onSecondaryContainer
        )
        else -> CardDefaults.cardColors()
    }
}

@Composable
private fun taskPriorityAccentColor(priority: String) = when (priority.uppercase()) {
    "HIGH" -> MaterialTheme.colorScheme.error
    "MEDIUM" -> MaterialTheme.colorScheme.tertiary
    "LOW" -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.outline
}

@Composable
private fun CreateZoneDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, description: String?, participatesCount: Int?) -> Unit,
    errorText: String?
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var countStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая зона") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = countStr,
                    onValueChange = { countStr = it },
                    label = { Text("Участников (число)") },
                    modifier = Modifier.fillMaxWidth()
                )
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            countStr.toIntOrNull()
                        )
                    }
                }
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}

@Composable
private fun CreateTaskDialog(
    zones: List<Zone>,
    participants: List<ParticipantApiModel>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, zoneId: String?, priority: String?, deadline: String?, performerIds: List<String>?) -> Unit,
    errorText: String?
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadlineAt by remember { mutableStateOf<LocalDateTime?>(null) }
    var priority by remember { mutableStateOf("MEDIUM") }
    var priorityExpanded by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var selectedZone by remember { mutableStateOf<Zone?>(null) }
    var selectedPerformerIds by remember { mutableStateOf(setOf<String>()) }

    val performerCandidates = remember(participants) {
        participants.filter { it.role == ROLE_PERFORMER }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая задача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название *") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedZone?.name ?: "Без зоны",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Зона (необязательно)") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать зону")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Без зоны") },
                            onClick = {
                                selectedZone = null
                                expanded = false
                            }
                        )
                        zones.forEach { zone ->
                            DropdownMenuItem(
                                text = { Text(zone.name) },
                                onClick = {
                                    selectedZone = zone
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = taskPriorityRu(priority),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Приоритет") },
                        trailingIcon = {
                            IconButton(onClick = { priorityExpanded = !priorityExpanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать приоритет")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { priorityExpanded = true }
                    )
                    DropdownMenu(
                        expanded = priorityExpanded,
                        onDismissRequest = { priorityExpanded = false }
                    ) {
                        TASK_PRIORITY_OPTIONS.forEach { code ->
                            DropdownMenuItem(
                                text = { Text(taskPriorityRu(code)) },
                                onClick = {
                                    priority = code
                                    priorityExpanded = false
                                }
                            )
                        }
                    }
                }
                DateTimePickerField(
                    value = deadlineAt,
                    onValueChange = { deadlineAt = it },
                    label = "Дедлайн",
                    modifier = Modifier.fillMaxWidth(),
                    allowClear = true
                )
                Text(
                    "Исполнители (${participationRoleRu("PERFORMER")})",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
                if (performerCandidates.isEmpty()) {
                    Text(
                        "В мероприятии нет участников с ролью исполнителя — назначить никого нельзя (по правилам сервера).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    performerCandidates.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedPerformerIds.contains(p.userId),
                                onCheckedChange = { checked ->
                                    selectedPerformerIds = if (checked) {
                                        selectedPerformerIds + p.userId
                                    } else {
                                        selectedPerformerIds - p.userId
                                    }
                                }
                            )
                            Text(
                                text = p.email,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
                errorText?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        val ids = selectedPerformerIds.toList().takeIf { it.isNotEmpty() }
                        onConfirm(
                            title.trim(),
                            description.trim().takeIf { it.isNotEmpty() },
                            selectedZone?.id,
                            priority.takeIf { it.isNotBlank() },
                            deadlineAt?.toApiDateTimeString(),
                            ids
                        )
                    }
                }
            ) { Text("Создать") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        }
    )
}
