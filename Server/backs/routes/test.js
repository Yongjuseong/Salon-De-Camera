const express=require('express');
const router=express.Router();

router.get('/',(req,res)=>{
	res.send({
		"result":"done"
	});
	console.log('work');
});

module.exports=router;
