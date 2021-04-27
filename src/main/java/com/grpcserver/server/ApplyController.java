package com.grpcserver.server;

import com.student.constants.ServiceResult;
import com.student.constants.SystemConstant;
import com.student.models.*;
import com.student.service.*;
import com.student.utils.HttpUtils;
import com.student.utils.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/apply")
public class ApplyController {
    @Autowired
    private ApplyService applyService;

    @Autowired
    private TypeService typeService;

    @Autowired
    private CommendService commendService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private ComplainService complainService;

    @RequestMapping(value = "/applyLists/{applyType}/{pageNumber}", method = RequestMethod.GET)
    public ModelAndView applyLists(@PathVariable int applyType,
                                   @PathVariable int pageNumber,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   ModelMap modelMap) {
        HttpUtils.checkIfLogedIn(request, response, false);
        List<ApplyVO> applys = null;
        int total = 0;
        Page page = new Page();

        //只能看自己的
        HttpUtils.checkIfLogedIn(request, response, false);
        int accountId = Integer.parseInt(request.getSession().getAttribute("accountId").toString());
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());

        UserVO userVO = new UserVO();
        userVO.setAccountId(accountId);

        if (userType == 0) {
            userVO.setAccountId(-3);
        }

        ApplyVO searchBean = new ApplyVO();
        searchBean.setStatus(-2);
        searchBean.setUserVO(userVO);
        searchBean.setApplyType(applyType);

        String suffix = applyType + "/";

        try {
            total = applyService.pageListApplysNewTotal(searchBean);
            page = PageUtil.processPage(pageNumber, total, "/apply/applyLists/", suffix);
            applys = applyService.pageListApplysNew(page.getStart(), PageUtil.PAGE_SIZE, searchBean);
        } catch (Exception e) {
            e.printStackTrace();
        }

        modelMap.put("applys", applys);
        modelMap.put("page", page);
        modelMap.put("templateInfo", "apply/applyLists.html");
        modelMap.put("userType", userType);

        modelMap.put("applyType", applyType);

        if (applyType == 1) {
            modelMap.put("apply1Active", "active");
        } else if (applyType == 2) {
            modelMap.put("apply2Active", "active");
        } else if (applyType == 3) {
            modelMap.put("apply3Active", "active");
        }

        return HttpUtils.mvcNewExtend(request, modelMap);
    }


    @RequestMapping(value = "/verifyApplys/{applyType}/{pageNumber}", method = RequestMethod.GET)
    public ModelAndView verifyApplys(@PathVariable int applyType,
                                     @PathVariable int pageNumber,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap) {
        ApplyVO searchBean = new ApplyVO();
        searchBean.setStatus(SystemConstant.WAITING_VERIFY);
        searchBean.setApplyType(applyType);
        int total = 0;
        Page page = new Page();
        List<ApplyVO> applys = new ArrayList<ApplyVO>();
        try {
            total = applyService.pageListApplysNewTotal(searchBean);
            page = PageUtil.processPage(pageNumber, total, "/apply/applyLists/", "");
            applys = applyService.pageListApplysNew(page.getStart(), PageUtil.PAGE_SIZE, searchBean);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        modelMap.put("userType", userType);

        modelMap.put("applys", applys);

        modelMap.put("applyType", applyType);

        if (applyType == 1) {
            modelMap.put("verifyApply1Active", "active");
        } else if (applyType == 2) {
            modelMap.put("verifyApply2Active", "active");
        } else if (applyType == 3) {
            modelMap.put("verifyApply3Active", "active");
        }

        modelMap.put("page", page);
        modelMap.put("templateInfo", "apply/verifyApplys.html");
        modelMap.put("verifyApplysActive", "active");
        return HttpUtils.mvcNewExtend(request, modelMap);
    }


    @RequestMapping(value = "/readApplys/{pageNumber}", method = RequestMethod.GET)
    public ModelAndView readApplys(@PathVariable int pageNumber, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        HttpUtils.checkIfLogedIn(request, response, false);
        List<ApplyVO> applys = null;
        int total = 0;
        Page page = new Page();

        ApplyVO searchBean = new ApplyVO();
        searchBean.setApplyType(-2);
        searchBean.setStatus(SystemConstant.VERIFIED);
        try {
            total = applyService.pageListApplysNewTotal(searchBean);
            page = PageUtil.processPage(pageNumber, total, "/apply/readApplys/", "");
            applys = applyService.pageListApplysNew(page.getStart(), PageUtil.PAGE_SIZE, searchBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        modelMap.put("userType", userType);

        modelMap.put("applys", applys);
        modelMap.put("page", page);
        modelMap.put("templateInfo", "apply/readApplys.html");
        modelMap.put("readApplysActive", "active");
        return HttpUtils.mvcNewExtend(request, modelMap);
    }


    @RequestMapping(value = "/verifyApplyPage/{applyId}", method = RequestMethod.GET)
    public ModelAndView verifyApplyPage(@PathVariable int applyId,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        ModelMap modelMap) {
        HttpUtils.checkIfLogedIn(request, response, false);
        ApplyVO applyVO = new ApplyVO();
        //判断是新增还是修改
        //更新
        ApplyVO searchBean = new ApplyVO();
        searchBean.setApplyId(applyId);
        searchBean.setApplyType(-2);
        searchBean.setStatus(SystemConstant.WAITING_VERIFY);
        try {
            List<ApplyVO> applys = applyService.pageListApplysNew(0, 1, searchBean);
            applyVO = applys.get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        modelMap.put("userType", userType);

        int applyType = applyVO.getApplyType();

        modelMap.put("applyType", applyType);

        if (applyType == 1) {
            modelMap.put("verifyApply1Active", "active");
        } else if (applyType == 2) {
            modelMap.put("verifyApply2Active", "active");
        } else if (applyType == 3) {
            modelMap.put("verifyApply3Active", "active");
        }

        modelMap.put("applyVO", applyVO);


        modelMap.put("templateInfo", "apply/verifyApplyPage.html");
        return HttpUtils.mvcNewExtend(request, modelMap);
    }


    @RequestMapping(value = "/applyAddAndEdit/{applyType}/{applyId}", method = RequestMethod.GET)
    public ModelAndView applyAddAndEdit(@PathVariable int applyType,
                                        @PathVariable int applyId,
                                        HttpServletRequest request,
                                        HttpServletResponse response,
                                        ModelMap modelMap) {
        HttpUtils.checkIfLogedIn(request, response, false);
        ApplyVO applyVO = new ApplyVO();
        //判断是新增还是修改
        if (applyId > 0) {
            //更新
            ApplyVO searchBean = new ApplyVO();
            searchBean.setApplyId(applyId);
            searchBean.setApplyType(-2);
            try {
                List<ApplyVO> applys = applyService.pageListApplysNew(0, 1, searchBean);
                applyVO = applys.get(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //增加
        }

        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        modelMap.put("userType", userType);

        modelMap.put("applyType", applyType);

        if (applyType == 1) {
            modelMap.put("apply1Active", "active");
        } else if (applyType == 2) {
            modelMap.put("apply2Active", "active");
        } else if (applyType == 3) {
            modelMap.put("apply3Active", "active");
        }

        modelMap.put("applyVO", applyVO);
        modelMap.put("templateInfo", "apply/applyAddAndEdit.html");
        return HttpUtils.mvcNewExtend(request, modelMap);
    }


    @RequestMapping(value = "/applyContent/{applyId}", method = RequestMethod.GET)
    public ModelAndView applyContent(@PathVariable int applyId,
                                     HttpServletRequest request,
                                     HttpServletResponse response,
                                     ModelMap modelMap) {
        HttpUtils.checkIfLogedIn(request, response, false);
        ApplyVO applyVO = new ApplyVO();
        CommendVO commendVO = new CommendVO();
        ApplyVO searchBean = new ApplyVO();
        searchBean.setStatus(-3);
        UserVO userVO = new UserVO();
        userVO.setAccountId(-2);
        searchBean.setUserVO(userVO);
        searchBean.setApplyId(applyId);


        //文章类型列表
        List<TypeVO> types = new ArrayList<TypeVO>();
        try {
            types = typeService.pageListTypesNew(0, 100, new TypeVO());
        } catch (Exception e) {
            e.printStackTrace();
        }

        int userType = Integer.parseInt(request.getSession().getAttribute("userType").toString());
        modelMap.put("userType", userType);
        modelMap.put("types", types);
        modelMap.put("applyVO", applyVO);
        modelMap.put("templateInfo", "apply/applyContent.html");
        return HttpUtils.mvcNewExtend(request, modelMap);
    }

    @RequestMapping(value = "/delete/{rowId}", method = RequestMethod.GET)
    public @ResponseBody
    JsonResult deleteApply(@PathVariable int rowId,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           ModelMap modelMap) {
        JsonResult jsonResult = new JsonResult();
        ApplyVO applyVO = new ApplyVO();
        applyVO.setApplyId(rowId);
        try {
            applyService.deleteApplyById(applyVO);
            jsonResult.setResult(ServiceResult.SUCCESSED);
        } catch (Exception e) {
            jsonResult.setResult(ServiceResult.FAILED);
            e.printStackTrace();
        }
        return jsonResult;
    }


    @RequestMapping(value = "/submitToVerify/{applyId}", method = RequestMethod.GET)
    public @ResponseBody
    JsonResult submitToVerify(@PathVariable int applyId,
                              HttpServletRequest request,
                              HttpServletResponse response,
                              ModelMap modelMap) {
        JsonResult jsonResult = new JsonResult();
        ApplyVO applyVO = new ApplyVO();
        applyVO.setApplyId(applyId);
        applyVO.setApplyType(-2);
        applyVO.setStatus(SystemConstant.EDITING);
        try {
            List<ApplyVO> applyVOList = applyService.pageListApplysNew(0, 1, applyVO);
            ApplyVO applyWantToVerify = applyVOList.get(0);
            applyWantToVerify.setStatus(SystemConstant.WAITING_VERIFY);
            applyService.updateApplyById(applyWantToVerify);
            jsonResult.setResult(ServiceResult.SUCCESSED);
        } catch (Exception e) {
            jsonResult.setResult(ServiceResult.FAILED);
            e.printStackTrace();
        }
        return jsonResult;
    }


    @RequestMapping(value = "/saveAndEditSubmit", method = RequestMethod.POST)
    public @ResponseBody
    JsonResult saveAndEditSubmit(@RequestBody ApplyVO applyVO, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        JsonResult jsonResult = new JsonResult();
        HttpUtils.checkIfLogedIn(request, response, false);
        int accountId = Integer.parseInt(request.getSession().getAttribute("accountId").toString());
        UserVO userVO = new UserVO();
        userVO.setAccountId(accountId);
        applyVO.setUserVO(userVO);

        if (applyVO.getApplyName() == null || applyVO.getApplyName().trim().length() == 0) {
            jsonResult.setResult(ServiceResult.ARTICLE_TITLE_CANNOT_BE_NULL);
            return jsonResult;
        }
        try {
            ApplyVO applyVOCopy = applyVO;
            int result = 0;

            String saveFile = HttpUtils.getDefaultUploadFolder(request);

            if (applyVO.getApplyId() < 1) {
                //add
                result = applyService.addApplyNew(applyVO);
                jsonResult.setResult(ServiceResult.SUCCESSED);
            } else {
                result = applyService.updateApplyById(applyVO);
                result = applyVO.getApplyId();
                jsonResult.setResult(ServiceResult.SUCCESSED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResult.setResult(ServiceResult.FAILED);
        }
        return jsonResult;
    }

    @RequestMapping(value = "/verifySubmit", method = RequestMethod.POST)
    public @ResponseBody
    JsonResult verifySubmit(@RequestBody ApplyVO applyVO, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        JsonResult jsonResult = new JsonResult();
        HttpUtils.checkIfLogedIn(request, response, false);
        ApplyVO applyVOCopy = applyVO;

        ApplyVO searchBean = new ApplyVO();
        searchBean.setApplyId(applyVO.getApplyId());
        searchBean.setApplyType(-2);
        searchBean.setStatus(SystemConstant.WAITING_VERIFY);
        int result = 0;
        try {
            List<ApplyVO> applys = applyService.pageListApplysNew(0, 1, searchBean);
            applyVOCopy = applys.get(0);
            applyVOCopy.setStatus(applyVO.getStatus());
            applyVOCopy.setReason(applyVO.getReason());
            applyVOCopy.setColumn8(applyVO.getColumn8());
            result = applyService.updateApplyById(applyVOCopy);
            jsonResult.setResult(ServiceResult.SUCCESSED);
        } catch (Exception e) {
            e.printStackTrace();
            jsonResult.setResult(ServiceResult.FAILED);
        }
        return jsonResult;
    }

}
