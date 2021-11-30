const express=require('express');
const router=express.Router();
const fs=require('fs');
const path=require('path');
const multer=require('multer');
const mysql=require('mysql');
const ps=require('python-shell');

const myDB=mysql.createConnection({
        host:'localhost',
        port:3306,
        user:'root',
        password:'backs',
        database:'sdc'
});
					
var tmp;
var options={},options2={};
const upload=multer({
	storage: multer.diskStorage({
		destination:function(req,file,cb){
			cb(null,'uploads/');
		},
		filename:function(req,file,cb){
			myDB.query('SELECT * FROM filename',(err,results)=>{
				console.log(results);
				tmp=results.length+1;
				myDB.query('INSERT INTO filename values(?)',(tmp),(err,results)=>{
					cb(null,"img"+tmp.toString()+".jpg");
					options={
						mode:'text',
						pythonPath:'',
						pythonOptions:['-u'],
						scriptPath:'',
						args:['uploads/img'+tmp.toString()+".jpg"]
					};
					options2={
						mode:'text',
						pythonPath:'',
						pythonOptions:['-u'],
						scriptPath:'',
						args:['mask','--image=uploads/img'+tmp.toString()+".jpg",'--weights=Hair-Detection/mask_rcnn_hair_0200.h5']
					};
				});
			});
		}
	}),	
});

router.post('/',upload.single('img'),(req,res)=>{
	console.log(req.file);
	ps.PythonShell.run('face_detection.py',options,(err,results)=>{
		if(err) console.log(err);
		console.log('results: %j', results);
		ps.PythonShell.run('Hair-Detection/run.py',options2,(err,results2)=>{
			if(err) console.log(err);
			console.log('results2: %j', results2);
			console.log('Done');
			fs.readFile("crop_hair.png",(err,data)=>{			
				res.writeHead(200,{"Context-Type":"image/png"});
				res.write(data);
				res.end();
				console.log("start");
			});
		});
	});
});

router.get('/',(req,res)=>{
	fs.readFile('./xy.txt','utf8',(err,results)=>{
		fs.readFile('./size.txt','utf8',(err2,results2)=>{
			var tmp=results+results2;
			res.send(tmp);
			console.log(tmp);
		});
	});
});


module.exports=router;
