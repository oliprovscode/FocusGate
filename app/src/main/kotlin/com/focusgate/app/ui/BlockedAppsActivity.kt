package com.focusgate.app.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.focusgate.app.R
import com.focusgate.app.data.BlockedAppsManager
import com.focusgate.app.databinding.ActivityBlockedAppsBinding

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Drawable,
    var isBlocked: Boolean
)

class BlockedAppsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockedAppsBinding
    private val allApps = mutableListOf<AppInfo>()
    private val filtered = mutableListOf<AppInfo>()
    private lateinit var adapter: AppsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockedAppsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        adapter = AppsAdapter(filtered) { appInfo, checked ->
            if (checked) {
                BlockedAppsManager.addPackage(this, appInfo.packageName)
            } else {
                BlockedAppsManager.removePackage(this, appInfo.packageName)
            }
            appInfo.isBlocked = checked
        }
        binding.rvApps.layoutManager = LinearLayoutManager(this)
        binding.rvApps.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadApps()
    }

    private fun loadApps() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvApps.visibility = View.GONE

        Thread {
            val pm = packageManager
            val blockedSet = BlockedAppsManager.getBlockedPackages(this)

            val installed = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 } // user-installed only
                .map { info ->
                    AppInfo(
                        label = pm.getApplicationLabel(info).toString(),
                        packageName = info.packageName,
                        icon = pm.getApplicationIcon(info),
                        isBlocked = blockedSet.contains(info.packageName)
                    )
                }
                .sortedWith(compareByDescending<AppInfo> { it.isBlocked }.thenBy { it.label })

            allApps.clear()
            allApps.addAll(installed)

            runOnUiThread {
                filterApps(binding.etSearch.text?.toString() ?: "")
                binding.progressBar.visibility = View.GONE
                binding.rvApps.visibility = View.VISIBLE
            }
        }.start()
    }

    private fun filterApps(query: String) {
        filtered.clear()
        if (query.isBlank()) {
            filtered.addAll(allApps)
        } else {
            val q = query.lowercase()
            filtered.addAll(allApps.filter {
                it.label.lowercase().contains(q) || it.packageName.lowercase().contains(q)
            })
        }
        adapter.notifyDataSetChanged()
    }
}

class AppsAdapter(
    private val items: List<AppInfo>,
    private val onToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppsAdapter.VH>() {

    inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivAppIcon)
        val label: TextView = view.findViewById(R.id.tvAppLabel)
        val pkg: TextView = view.findViewById(R.id.tvAppPackage)
        val toggle: Switch = view.findViewById(R.id.switchBlock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.icon.setImageDrawable(item.icon)
        holder.label.text = item.label
        holder.pkg.text = item.packageName
        holder.toggle.setOnCheckedChangeListener(null)
        holder.toggle.isChecked = item.isBlocked
        holder.toggle.setOnCheckedChangeListener { _, checked ->
            onToggle(item, checked)
        }
    }

    override fun getItemCount() = items.size
}
