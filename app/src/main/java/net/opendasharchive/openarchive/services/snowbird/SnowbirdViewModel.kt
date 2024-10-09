package net.opendasharchive.openarchive.services.snowbird

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.opendasharchive.openarchive.db.ApiError
import net.opendasharchive.openarchive.db.SnowbirdAPI
import net.opendasharchive.openarchive.db.SnowbirdGroup
import net.opendasharchive.openarchive.features.main.ApiResponse
import net.opendasharchive.openarchive.util.BaseViewModel
import net.opendasharchive.openarchive.util.trackProcessingWithTimeout
import timber.log.Timber
import java.util.UUID

data class MockDataItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null
)

object MockData {
    val documents = listOf(
        MockDataItem(
            title = "splinternet_report_final_draft.pdf",
            description = "4.1 MB"
        ),
        MockDataItem(
            title = "DHS Study on Mobile Device Security",
            description = "3.4 MB"
        ),
        MockDataItem(
            title = "The Blue Team Handbook",
            description = "2.6 MB"
        ),
        MockDataItem(
            title = "NIST Cybersecurity Framework",
            description = "1.3 MB"
        ),
        MockDataItem(
            title = "The CIS Critical Security Controls",
            description = "916 KB"
        ),
    )

    val foo = listOf(
        MockDataItem(
            title = "Myanmar Cultural Heritage",
            description =  "Document and digitally preserve Myanmar's diverse cultural heritage."
        ),
        MockDataItem(
            title = "Whistleblower Interview Series",
            description = "Conduct and document a series of interviews with corporate and government whistleblowers."
        ),
        MockDataItem(
            title = "Lost Civil War Letters Recovery",
            description = "Locate, transcribe, and archive a collection of recently discovered Civil War-era correspondence."
        ),
        MockDataItem(
            title = "Tech Giant Data Practices Exposé",
            description = "Investigate and report on data collection and usage practices of major technology companies."
        ),
        MockDataItem(
            title = "Indigenous Oral History Project",
            description = "Record and preserve oral histories from indigenous communities across the country."
        ),
        MockDataItem(
            title = "Cold War Declassified Documents Analysis",
            description = "Analyze and summarize newly declassified documents from the Cold War era."
        ),
        MockDataItem(
            title = "Climate Change Impact Documentation",
            description = "Document and report on the tangible effects of climate change in various ecosystems."
        ),
        MockDataItem(
            title = "Forgotten Inventors' Patents Catalog",
            description = "Research and compile a catalog of overlooked inventions and their creators from the past century."
        ),
        MockDataItem(
            title = "Global Street Art Photography Collection",
            description = "Photograph and document street art from cities around the world, exploring cultural expressions and social commentary."
        ),
        MockDataItem(
            title = "Pandemic Response Audio Diary Compilation",
            description = "Collect and curate audio diaries from individuals worldwide, documenting personal experiences during the global pandemic."
        )
    )

    val groups = listOf(
        MockDataItem(
            title = "Human Trafficking",
            description = "Investigating and documenting cases of human trafficking, aiding law enforcement and raising public awareness."
        ),
        MockDataItem(
            title = "Corruption",
            description = "Uncovering and reporting on corruption in government and corporate sectors globally."
        ),
        MockDataItem(
            title = "Environmental Crime",
            description = "Focusing on exposing illegal activities that harm the environment, such as illegal logging or wildlife trafficking."
        ),
        MockDataItem(
            title = "Cybercrime",
            description = "Specializing in tracking and analyzing digital crimes, from identity theft to large-scale hacking operations."
        ),
        MockDataItem(
            title = "Hate Crime",
            description = "Collecting data and reporting on hate crimes, advocating for victim support and policy changes."
        ),
        MockDataItem(
            title = "Forced Labor",
            description = "Investigating and exposing instances of forced labor and modern slavery in global supply chains."
        ),
        MockDataItem(
            title = "Child Exploitation",
            description = "Working to identify and combat various forms of child exploitation, including online predators and child labor."
        ),
        MockDataItem(
            title = "Refugee Rights",
            description = "Monitoring the treatment of refugees and asylum seekers, reporting on rights violations and advocating for humane policies."
        ),
        MockDataItem(
            title = "Political Prisoners",
            description = "Documenting cases of political imprisonment and advocating for the rights and release of political prisoners."
        )
    )

    val repos = listOf(
        MockDataItem(
            id = "MALW001",
            title = "Malware Analysis",
            description = "Tools and techniques for analyzing and reverse engineering malicious software."
        ),
        MockDataItem(
            id = "PHISH002",
            title = "Phishing Campaigns",
            description = "Tracking and analysis of phishing attempts and social engineering tactics."
        ),
        MockDataItem(
            id = "DDOS003",
            title = "DDoS Attack Vectors",
            description = "Study of Distributed Denial of Service attack methods and mitigation strategies."
        ),
        MockDataItem(
            id = "CRYPTO004",
            title = "Cryptojacking Operations",
            description = "Investigation into unauthorized use of systems for cryptocurrency mining."
        ),
        MockDataItem(
            id = "RANSOM005",
            title = "Ransomware Trends",
            description = "Tracking the evolution and impact of ransomware attacks across various sectors."
        ),
        MockDataItem(
            id = "DARKWEB006",
            title = "Dark Web Marketplaces",
            description = "Monitoring and analysis of illegal activities on dark web platforms."
        ),
        MockDataItem(
            id = "IDTHEFT007",
            title = "Identity Theft Techniques",
            description = "Research on methods used for stealing and exploiting personal information."
        ),
        MockDataItem(
            id = "FINFRAUD008",
            title = "Financial Fraud Schemes",
            description = "Investigation of cyber-enabled financial fraud and money laundering operations."
        ),
        MockDataItem(
            id = "BOTNET009",
            title = "Botnet Infrastructure",
            description = "Analysis of botnet architectures, command and control systems, and propagation methods."
        ),
        MockDataItem(
            id = "ZERODAY010",
            title = "Zero-Day Exploits",
            description = "Tracking and analysis of newly discovered and exploited software vulnerabilities."
        ),
        MockDataItem(
            id = "SOCENG011",
            title = "Social Engineering Tactics",
            description = "Study of psychological manipulation techniques used in cybercrime."
        ),
        MockDataItem(
            id = "ATTRB012",
            title = "Attribution Techniques",
            description = "Methods and challenges in attributing cybercrimes to specific actors or groups."
        )
    )

    val users = listOf(
        MockDataItem(
            title = "HumorousShepherd",
        ),
        MockDataItem(
            title = "ResilientCourier",
        ),
        MockDataItem(
            title = "HackerPeak",
        ),
        MockDataItem(
            title = "ProsperityAdvocate",
        ),
        MockDataItem(
            title = "ListlessRebel",
        ),
    )
}

class SnowbirdViewModel(val api: SnowbirdAPI) : BaseViewModel() {

    val status: StateFlow<SnowbirdServiceStatus> = SnowbirdBridge.getInstance().status
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SnowbirdServiceStatus.BackendInitializing
        )

    private val _group = MutableStateFlow<SnowbirdGroup?>(null)
    val group: StateFlow<SnowbirdGroup?> = _group.asStateFlow()

    private val _groups = MutableStateFlow<List<SnowbirdGroup>>(emptyList())
    val groups: StateFlow<List<SnowbirdGroup>> = _groups.asStateFlow()

    private val _repos = MutableStateFlow(MockData.repos)
    val repos: StateFlow<List<MockDataItem>> = _repos.asStateFlow()

    private val _documents = MutableStateFlow(MockData.documents)
    val documents: StateFlow<List<MockDataItem>> = _documents.asStateFlow()

    private val _users = MutableStateFlow(MockData.users)
    val users: StateFlow<List<MockDataItem>> = _users.asStateFlow()

    private val _error = MutableStateFlow<ApiError?>(null)
    val error: StateFlow<ApiError?> = _error.asStateFlow()

    var currentError: ApiError?
        get() = _error.value
        set(value) {
            _error.value = value
            Timber.d("Error set to $value")
        }

    fun uploadDocument(delaySeconds: Int = 5) {
        viewModelScope.launch {
            processingTracker.trackProcessingWithTimeout(10_000, "fetch_group") {
                delay(delaySeconds * 1000L)
                _documents.value += MockDataItem(title = "Newly Uploaded Document", description = "256 KB")
            }
        }
    }

//    fun searchGroupss(query: String) {
//        viewModelScope.launch {
//            _groups.value = MockData.groups.filter {
//                it.title.contains(query, ignoreCase = true) ||
//                        it.description.contains(query, ignoreCase = true)
//            }
//        }
//    }
//
//    fun sortGroupsByName() {
//        viewModelScope.launch {
//            _groups.value = _groups.value.sortedBy { it.name }
//        }
//    }
    
    fun fetchGroup(groupId: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_group") {
                    when (val response = api.fetchGroup(groupId)) {
                        is ApiResponse.SingleResponse -> _group.value = response.data
                        is ApiResponse.ErrorResponse -> _error.value = response.error
                        else -> _error.value = ApiError.UnexpectedError("Unexpected response type")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }

    fun fetchGroups() {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "fetch_groups") {
                    val response = api.fetchGroups()
                    Timber.d("response = $response")
                    when (response) {
                        is ApiResponse.ListResponse -> _groups.value = response.data
                        is ApiResponse.ErrorResponse -> currentError = response.error
                        else -> currentError = ApiError.UnexpectedError("Unexpected response type")
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }

    fun createGroup(groupName: String) {
        viewModelScope.launch {
            try {
                processingTracker.trackProcessingWithTimeout(10_000, "create_group") {
                    when (val response = api.createGroup(groupName)) {
                        is ApiResponse.SingleResponse -> {
                            Timber.d("response = $response")
                            _group.value = response.data
                            _groups.value += response.data
                        }

                        is ApiResponse.ErrorResponse -> {
                            Timber.d("response = $response")
                            _error.value = response.error
                        }

                        else -> {
                            Timber.d("error response = $response")
                            _error.value = ApiError.UnexpectedError("Unexpected response type")
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                _error.value = ApiError.TimedOut
            }
        }
    }
}