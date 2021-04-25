const save=document.querySelector('#save');
const inp=document.querySelector('#inp');
const ul=document.querySelector('ul');
let b=false;
let btn=document.querySelectorAll('.Mark');
save.onclick=Event=>{
        console.log('Outer Save');
        console.log(b);
        const new_li=document.createElement('li');
        const new_div=document.createElement('div');
        
        new_li.appendChild(new_div);
        const new_btn=document.createElement('button');
        new_btn.innerText='Mark done!';
        const new_bt=document.createElement('button');
        new_bt.innerText='Delete';
    
        const new_b=document.createElement('button');
        new_b.innerText='Edit';
    
        const divindiv=document.createElement('div');
        divindiv.innerText=inp.value;
        new_div.appendChild(divindiv);
        new_div.appendChild(new_btn);
        new_div.appendChild(new_bt);
        new_div.appendChild(new_b);
        new_li.classList.add('liclass');
        divindiv.classList.add('divindiv');
        new_div.classList.add('divinli');
        new_btn.classList.add('Mark');
        new_bt.classList.add('Delete');
        new_b.classList.add('Edit');
        ul.appendChild(new_li);
        new_btn.onclick=e=>{
            divindiv.classList.toggle('line');
            divsel=divindiv;
            if(new_btn.innerText==='Mark done!'){
                new_btn.innerText='Mark Undone';
            }
            else{
                new_btn.innerText='Mark done!';
            }
        }
        new_b.onclick=ev=>{
            if(b===false){
                console.log('Edit');
                inp.value=divindiv.innerText;
                b=true;
                new_b.innerText='Save Changes.';
            }

            else{
                console.log('INNER Save');
                console.log(b);
                divindiv.innerText=inp.value;
                inp.value="";
                b=false;
                new_b.innerText='Edit';
         
            }
        }
        new_bt.onclick=even=>{
            new_li.remove();
        }
    
    
    inp.value="";
    console.log(Event.target);
}

