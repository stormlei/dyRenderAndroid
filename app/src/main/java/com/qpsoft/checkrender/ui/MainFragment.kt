package com.qpsoft.checkrender.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.qpsoft.checkrender.R
import com.qpsoft.checkrender.common.Helper
import com.qpsoft.checkrender.data.model.ComboItem
import com.qpsoft.checkrender.databinding.FooterUpFilesBinding
import com.qpsoft.checkrender.databinding.FragmentMainBinding
import com.qpsoft.checkrender.utils.glide.GlideEngine
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject


@AndroidEntryPoint
class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java).apply {
            comboItemListLiveData.observe(this@MainFragment, {
                comboItemList = it
            })
        }

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_main, null, false)

        viewModel.comboItemList()
    }

    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return binding.root
    }

    private val comboItem = "完全矫正视力"
    private var comboItemList = mutableListOf<ComboItem>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btn.setOnClickListener {
            renderUI(comboItemList)
        }

        binding.submit.setOnClickListener {
            submitData(comboItemList)
        }
    }



    private fun renderUI(list: MutableList<ComboItem>) {
        val comboItem = list.find { it.name == comboItem } ?: return
        val showEyes: Boolean = comboItem.doubleEye
        val itemList = comboItem.items

        val childView: LinearLayout
        if (showEyes) {
            childView = layoutInflater.inflate(R.layout.view_odos, binding.rootView, false) as LinearLayout
        } else {
            childView = layoutInflater.inflate(R.layout.view_no_odos, binding.rootView, false) as LinearLayout
            val tvTitle = childView.findViewById<TextView>(R.id.tv_title)
            tvTitle.text = comboItem.name
        }

        var pos = -1
        for (item in itemList) {
            if (item.type == "vision" || item.type == "sph" || item.type == "cyl" || item.type == "axis" || item.type == "iop") {
                val itemView: View
                if (item.doubleEye) {
                    itemView = layoutInflater.inflate(R.layout.view_spe_odos, null)
                    val tvNameOd = itemView.findViewById<TextView>(R.id.tv_name_od)
                    val tvNameOs = itemView.findViewById<TextView>(R.id.tv_name_os)
                    tvNameOd.text = item.name
                    tvNameOs.text = item.name

                    val edtValueOd = itemView.findViewById<EditText>(R.id.edt_value_od)
                    val edtValueOs = itemView.findViewById<EditText>(R.id.edt_value_os)
                    edtValueOd.tag = item.item+item.key+"od"
                    edtValueOs.tag = item.item+item.key+"os"
                } else {
                    itemView = layoutInflater.inflate(R.layout.view_spe_no_odos, null)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = item.name

                    val edtValue = itemView.findViewById<EditText>(R.id.edt_value)
                    edtValue.tag = item.item+item.key
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }
            if (item.type == "radio") {
                val itemView = layoutInflater.inflate(R.layout.view_radio_no_odos, null)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name
                val rgView = itemView.findViewById<RadioGroup>(R.id.rg)
                for(str in item.optionList!!) {
                    val radioBtn = RadioButton(context)
                    radioBtn.text = str
                    rgView.addView(radioBtn)
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }

            if (item.type == "array") {
                val itemView = layoutInflater.inflate(R.layout.view_array_no_odos, null) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                val arrayView = itemView.findViewById<LinearLayout>(R.id.ll_array)

                val iList = item.itemList
                if (iList != null) {
                    for(it in iList) {
                        if (it.type == "select") {
                            val sView = layoutInflater.inflate(R.layout.view_array_select_no_odos, null)
                            val tvName = sView.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name
                            arrayView.addView(sView)
                        }
                        if (it.type == "template") {
                            val sView = layoutInflater.inflate(R.layout.view_array_select_no_odos, null)
                            val tvName = sView.findViewById<TextView>(R.id.tv_name)
                            tvName.text = it.name
                            arrayView.addView(sView)
                        }
                    }
                }

                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    if (suffixItem.type == "radio") {
                        val suffixView = layoutInflater.inflate(R.layout.view_suffix_radio_no_odos, null)
                        val tvName = suffixView.findViewById<TextView>(R.id.tv_name)
                        tvName.text = suffixItem.name
                        val rgView = suffixView.findViewById<RadioGroup>(R.id.rg)
                        for(str in suffixItem.optionList!!) {
                            val radioBtn = RadioButton(context)
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }

            if (item.type == "text") {
                val itemView = layoutInflater.inflate(R.layout.view_text_no_odos, null)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }

            if (item.type == "select") {
                val itemView = layoutInflater.inflate(R.layout.view_select_no_odos, null)
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }

            if (item.type == "template") {
                val itemView = layoutInflater.inflate(R.layout.view_template_no_odos, null) as LinearLayout
                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                tvName.text = item.name

                val str = item.template


                if (item.suffix != null) {
                    val suffixItem = item.suffix
                    if (suffixItem.type == "radio") {
                        val suffixView = layoutInflater.inflate(R.layout.view_suffix_radio_no_odos, null)
                        val tvName = suffixView.findViewById<TextView>(R.id.tv_name)
                        tvName.text = suffixItem.name
                        val rgView = suffixView.findViewById<RadioGroup>(R.id.rg)
                        for(str in suffixItem.optionList!!) {
                            val radioBtn = RadioButton(context)
                            radioBtn.text = str
                            rgView.addView(radioBtn)
                        }

                        itemView.addView(suffixView)
                    }
                }

                childView.addView(itemView)
                pos++
                if (pos % 2 != 0) {
                    itemView.setBackgroundColor(resources.getColor(R.color.color_0ff))
                }
            }
        }

        binding.rootView.addView(childView)

        if (comboItem.fileModule) {
            val childView = layoutInflater.inflate(R.layout.view_files, binding.rootView, false)
            binding.rootView.addView(childView)

            val picList = mutableListOf<String>()
            picList.add("ddddddd")
            val picAdapter = object: BaseQuickAdapter<String, BaseViewHolder>(R.layout.rv_files_item, picList) {
                override fun convert(holder: BaseViewHolder, item: String) {
                    val ivFile = holder.getView<ImageView>(R.id.iv_file)
                    Helper.loadFile(ivFile, item)
                }
            }
            val rvFiles = childView.findViewById<RecyclerView>(R.id.rv_files)
            rvFiles.layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
            rvFiles.adapter = picAdapter

            //footer layout
            val footerView = layoutInflater.inflate(R.layout.footer_up_files, binding.rootView, false)
            picAdapter.setFooterView(footerView)
            val footerUpFilesBinding = DataBindingUtil.bind<FooterUpFilesBinding>(footerView)!!
            footerUpFilesBinding.ivUpFiles.setOnClickListener {
                //cameraRequester.launch()
                PictureSelector.create(this@MainFragment)
                    .openGallery(PictureMimeType.ofImage())
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    .imageEngine(GlideEngine.createGlideEngine())
                    .maxSelectNum(1)
                    .isCompress(true)
                    .isEnableCrop(false)
                    .forResult(PictureConfig.CHOOSE_REQUEST)
            }
        }
        if (comboItem.remarkModule) {
            val childView = layoutInflater.inflate(R.layout.view_remark, binding.rootView, false)
            binding.rootView.addView(childView)
        }
    }



    private val jsonObj = JSONObject()
    private val dataObj = JSONObject()
    private fun submitData(list: MutableList<ComboItem>){
        val comboItem = list.find { it.name == comboItem } ?: return
        val itemList = comboItem.items
        for (item in itemList) {
            if (item.type == "vision" || item.type == "sph" || item.type == "cyl" || item.type == "axis" || item.type == "iop"){
                if (item.doubleEye) {
                    val edtValueOd = binding.rootView.findViewWithTag(item.item+item.key+"od") as EditText
                    val edtValueOs = binding.rootView.findViewWithTag(item.item+item.key+"os") as EditText
                    val ods = JSONObject()
                    ods.put("od", edtValueOd.text.toString())
                    ods.put("os", edtValueOs.text.toString())
                    dataObj.put(item.key, ods)
                } else {
                    val edtValue = binding.rootView.findViewWithTag(item.item+item.key) as EditText
                    dataObj.put(item.key, edtValue.text.toString())
                }
            }
            if (item.type == "radio"){
                //dataObj.put(item.key, binding.rootView.findd)
            }
            if (item.type == "text"){
                //dataObj.put(item.key, binding.rootView.findd)
            }
            if (item.type == "select"){
                //dataObj.put(item.key, binding.rootView.findd)
            }
        }
        jsonObj.put(comboItem.name, dataObj)

        LogUtils.e("-------$jsonObj")
    }


    private fun uploadFile(filePath: String) {
        //viewModel.uploadFile(filePath, "1")

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST ->{
                    val result = PictureSelector.obtainMultipleResult(data)
                    LogUtils.e("--------${result[0].compressPath}")
                    val filePath = result[0].compressPath
                    uploadFile(filePath)
                }
                else -> { }
            }
        }
    }
}