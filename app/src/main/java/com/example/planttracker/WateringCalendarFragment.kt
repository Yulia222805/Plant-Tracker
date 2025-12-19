//package com.example.planttracker
//
//import android.os.Bundle //для работы с жизненным циклом (onCreate и т.д.)
//import androidx.fragment.app.Fragment //от этого класса наследуются все фрагменты
//import android.view.LayoutInflater//чтобы "раздувать" макет из XML
//import android.view.View //базовый класс любого элемента интерфейса
//import android.view.ViewGroup //контейнер для View (например, FrameLayout)
////автоматически сгенерированный класс для связи с макетом
//import com.example.planttracker.databinding.FragmentPlantsBinding
//
//// TODO: Rename parameter arguments, choose names that match
//// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"
//
///**
// * A simple [Fragment] subclass.
// * Use the [PlantsFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
//class WateringCalendarFragment : Fragment() {
//    // TODO: Rename and change types of parameters
////    private var param1: String? = null
//    // временная переменная для хранения связи с макетом
//    private var _binding: FragmentPlantsBinding? = null
//    //    private var param2: String? = null
//    //"чистый" способ получить доступ к макету без проверки на null, я точно знаю, что не null!!
//    private val binding get() = _binding!!
//
////    override fun onCreate(
////        inflater: LayoutInflater, //инструмент для превращения XML в объекты
////        container: ViewGroup?, //родительский контейнер (может быть null)
////        savedInstanceState: Bundle? // сохранённое состояние (если фрагмент восстанавливается после поворота экрана)
////        ) {
////        super.onCreate(savedInstanceState)
////        arguments?.let {
////            param1 = it.getString(ARG_PARAM1)
////            param2 = it.getString(ARG_PARAM2)
////        }
////    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, //инструмент для превращения XML в объекты
//        container: ViewGroup?, //родительский контейнер (может быть null)
//        savedInstanceState: Bundle? // сохранённое состояние (если фрагмент восстанавливается после поворота экрана)
//    ): View? {
//        // Inflate the layout for this fragment
//        // inflate() — создаёт View из макета fragment_plants.xml
//        // false — означает: не добавлять его сразу в container (это сделает система позже)
//        _binding = FragmentPlantsBinding.inflate(inflater, container, false)
//        return binding.root
////        return inflater.inflate(R.layout.fragment_plants, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        // Устанавливаем текст в TextView с id="textView"
//        // Это заглушка, чтобы видеть, что фрагмент работает
//        binding.textView.text = "Экран: Календарь полива"
//    }
//
//    // Эта функция вызывается, когда фрагмент уничтожается
//    // Очищаем _binding, чтобы избежать утечки памяти
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
////    companion object {
////        /**
////         * Use this factory method to create a new instance of
////         * this fragment using the provided parameters.
////         *
////         * @param param1 Parameter 1.
////         * @param param2 Parameter 2.
////         * @return A new instance of fragment PlantsFragment.
////         */
////        // TODO: Rename and change types and number of parameters
////        @JvmStatic
////        fun newInstance(param1: String, param2: String) =
////            PlantsFragment().apply {
////                arguments = Bundle().apply {
////                    putString(ARG_PARAM1, param1)
////                    putString(ARG_PARAM2, param2)
////                }
////            }
////    }
//}