import tqdm
import torch
import numpy as np
from torch import nn
from torch import optim
from numpy import random
from models import KBCModel, CL
from regularizers import Regularizer
import torch.nn.functional as F
import torchvision.transforms as transforms


class KBCOptimizer(object):
    def __init__(
            self, dsl, ds, model_name, model: KBCModel, regularizer: list, optimizer: optim.Optimizer, batch_size: int = 256,
            temp: float = 1.0, rank: int = 2000, out_size: int = 2000, a_h: float = 0, a_t: float = 0,
            a_hr: float = 0, a_tr: float = 0, verbose: bool = True
    ):
        self.dsl = dsl
        self.ds = ds
        self.model_name = model_name
        self.model = model
        self.regularizer = regularizer[0]
        self.regularizer2 = regularizer[1]
        self.optimizer = optimizer
        self.batch_size = batch_size
        self.verbose = verbose
        self.temperature = temp
        self.rank = rank
        self.out_size = out_size
        self.a_h = a_h
        self.a_t = a_t
        self.a_hr = a_hr
        self.a_tr = a_tr
        self.nb = 1
    def get_pos(self, actual_examples, dic_hr=None, dic_tr=None, dic_h=None, dic_t=None):
        
        if dic_h is not None:
            p_h = []
            for i in actual_examples:
                h_sample = dic_h[(i[2].item(), i[1].item())]
                random.shuffle(h_sample)
                p_h.append(h_sample[0])

            return p_h
        if dic_t is not None:
            p_t = []
            for i in actual_examples:
                t_sample = dic_t[(i[0].item(), i[1].item())]
                random.shuffle(t_sample)

                p_t.append(t_sample[0])
            return p_t
    def get_neg(self, actual_examples, dic_hr=None, dic_tr=None, dic_h=None, dic_t=None):
        if dic_h is not None:
            n_h = []
            for i in actual_examples:
#                 h_sample = dic_h[(i[2].item(), i[1].item())]
#                 negs = set([i for i in range(14541)]) - set(h_sample)
#                 negs = list(negs)
#                 random.shuffle(negs)
                n_h += [x for x in range(self.nb)]

            return n_h
    def epoch(self, examples: torch.LongTensor, e=0, weight=None, dic_hr=None, dic_tr=None, dic_t=None, dic_h=None):
        self.model.train()
        actual_examples = examples[torch.randperm(examples.shape[0]), :]
        loss = nn.CrossEntropyLoss(reduction='mean', weight=weight)
        loss1 = nn.CrossEntropyLoss(reduction='mean')
      
        p_h = None
     
        pos_h_loss = 0


   
        if self.a_h != 0:
#             p_h , p_h_1= self.get_pos(actual_examples, dic_h=dic_h)
#             p_h ,p_h_1= torch.tensor(p_h),torch.tensor(p_h_1)
            p_h = self.get_pos(actual_examples, dic_h=dic_h)
            p_h = torch.tensor(p_h)
        
            n_h = self.get_neg(actual_examples, dic_h=dic_h)
            n_h = torch.tensor(n_h)

        if self.model_name == 'ComplEx':
            cl_net = CL(self.rank * 2, self.temperature, self.out_size).cuda()
        else:
            cl_net = CL(self.rank, self.temperature, self.out_size).cuda()
        with tqdm.tqdm(total=examples.shape[0], unit='ex', disable=not self.verbose) as bar:
            bar.set_description(f'train loss')
            b_begin = 0
            while b_begin < examples.shape[0]:
                input_batch = actual_examples[
                              b_begin:b_begin + self.batch_size
                              ].cuda()
                truth = input_batch[:, 2]
                r_truth = input_batch[:, 1]
                predictions, rel_pred, factors = self.model.forward(input_batch, mod=1)
                if self.ds == self.dsl[1]:
                    l_fit = loss(predictions, truth)+loss1(rel_pred, r_truth)
                else:
                    l_fit = loss(predictions, truth)
                l_reg = self.regularizer.forward(factors)

               
                if p_h is not None:
                    p_h_pos = p_h[b_begin:b_begin + self.batch_size].cuda()
                    
                    self_h_e = self.model.embeddings[0](input_batch[:, 0])
                    # 每个数据只取了一个正样本，为了降低计算量?由于选择是随机的，不排除
                    # 若干个epoch之后每个正样本都被选中了
                    p_h_e = self.model.embeddings[0](p_h_pos)
                    # 标签是真实的
                    
                    p_h_n = n_h[b_begin*self.nb:(b_begin+ self.batch_size)*self.nb].cuda()
                    
                    phne =  self.model.embeddings[0](p_h_n)
                    
                    labels1 = input_batch[:, 0]
                    pos_h_loss = cl_net(self_h_e, p_h_e,phne, labels1)
                 
                    # pos_h_loss = cl_net(self_h_e, p_h_e)
               
                l = l_fit + l_reg + self.a_h * pos_h_loss 

                self.optimizer.zero_grad()
                l.backward()
                self.optimizer.step()
                b_begin += self.batch_size
                bar.update(input_batch.shape[0])
                bar.set_postfix(loss=f'{l.item():.1f}')
        return l
